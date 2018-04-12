package com.yoshione.fingen;

import android.content.Context;
import android.support.test.InstrumentationRegistry;

import android.util.Log;

import com.yoshione.fingen.dao.AccountsDAO;
import com.yoshione.fingen.dao.CabbagesDAO;
import com.yoshione.fingen.dao.PayeesDAO;
import com.yoshione.fingen.dao.SendersDAO;
import com.yoshione.fingen.dao.SmsMarkersDAO;
import com.yoshione.fingen.model.Account;
import com.yoshione.fingen.model.Cabbage;
import com.yoshione.fingen.model.Payee;
import com.yoshione.fingen.model.Sender;
import com.yoshione.fingen.model.Sms;
import com.yoshione.fingen.model.SmsMarker;
import com.yoshione.fingen.model.Transaction;
import com.yoshione.fingen.utils.CabbageFormatter;
import com.yoshione.fingen.utils.SmsParser;

import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import au.com.bytecode.opencsv.CSV;
import au.com.bytecode.opencsv.CSVReadProc;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 * Created by Leonid on 14.02.2016.
 * 1
 */
public class SmsParserTest {
    private static final String TAG = "SmsParserTest";

    private List<SmsTest> smsTests;

    @Test

    public void testName() throws Exception {

    }

    @Test
    public void smsParser_CorrectParseSms() {
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();

        loadCSV();

        DBHelper.getInstance(context).clearDB();
        Cabbage cabbage1 = null;
        Cabbage cabbage2 = null;
        Account account = null;
        Account destAccount = null;
        Payee payee = null;
        try {
            cabbage1 = (Cabbage) CabbagesDAO.getInstance(context).createModel(new Cabbage(-1, "CB1", "CB1", "Cabbage1", 2));
            cabbage2 = (Cabbage) CabbagesDAO.getInstance(context).createModel(new Cabbage(-1, "CB2", "CB2", "Cabbage2", 2));
            account = (Account) AccountsDAO.getInstance(context).createModel(new Account(-1, "Account", cabbage1.getID(), "", "", BigDecimal.ZERO,
                    Account.AccountType.atCash, 0, false, BigDecimal.ZERO, BigDecimal.ZERO, 0, BigDecimal.ZERO));
            destAccount = (Account) AccountsDAO.getInstance(context).createModel(new Account(-1, "AccountDest", cabbage1.getID(), "", "", BigDecimal.ZERO,
                    Account.AccountType.atCash, 0, false, BigDecimal.ZERO, BigDecimal.ZERO, 0, BigDecimal.ZERO));
            payee = (Payee) PayeesDAO.getInstance(context).createModel(new Payee(-1, "Payee", -1, -1, 0, false));
        } catch (Exception e) {
            e.printStackTrace();
        }
        SmsMarkersDAO markersDAO = SmsMarkersDAO.getInstance(context);
        Sender sender;

        int k = 0;
        for (SmsTest smsTest : smsTests) {
            Log.d(TAG, String.format("SMS No %d", ++k));
            try {
                markersDAO.createModel(new SmsMarker(-1, SmsParser.MARKER_TYPE_ACCOUNT, String.valueOf(account.getID()), smsTest.mAccountMarker));
                markersDAO.createModel(new SmsMarker(-1, SmsParser.MARKER_TYPE_TRTYPE, String.valueOf(smsTest.mType), smsTest.mTypeMarker));
            } catch (Exception e) {
                e.printStackTrace();
            }

            switch (smsTest.mType) {
                case Transaction.TRANSACTION_TYPE_INCOME:
                case Transaction.TRANSACTION_TYPE_EXPENSE:
                    try {
                        markersDAO.createModel(new SmsMarker(-1, SmsParser.MARKER_TYPE_PAYEE, String.valueOf(payee.getID()), smsTest.mPayee));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
                case Transaction.TRANSACTION_TYPE_TRANSFER:
                    try {
                        markersDAO.createModel(new SmsMarker(-1, SmsParser.MARKER_TYPE_DESTACCOUNT, String.valueOf(destAccount.getID()), smsTest.mPayee));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
            }

            if (!smsTest.mCabbageAmount.isEmpty()) {
                try {
                    markersDAO.createModel(new SmsMarker(-1, SmsParser.MARKER_TYPE_CABBAGE, String.valueOf(cabbage1.getID()), smsTest.mCabbageAmount));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (!smsTest.mCabbageBalance.isEmpty()) {
                if (!smsTest.mCabbageAmount.equals(smsTest.mCabbageBalance)) {
                    try {
                        markersDAO.createModel(new SmsMarker(-1, SmsParser.MARKER_TYPE_CABBAGE, String.valueOf(cabbage2.getID()), smsTest.mCabbageBalance));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            try {
                sender = (Sender) SendersDAO.getInstance(context).createModel(new Sender(-1, "Bank" + String.valueOf(k), true, "900" + String.valueOf(k), smsTest.mAmountPos, smsTest.mBalancePos, "", false));

                SmsParser smsParser = new SmsParser(new Sms(1, new Date(), sender.getID(), smsTest.mText), context);

                //тест извлечения счета
                if (smsTest.mAccountMarker != null) {
                    Log.d(TAG, String.format("Account %s with marker %s extracted (id = %d)", account.getName(), smsTest.mAccountMarker, account.getID()));
                }
                assertThat(smsParser.extractAccount().getID(), is(account.getID()));


                //тест извлечения типа
                Log.d(TAG, String.format("Type %d with marker %s extracted", smsTest.mType, smsTest.mTypeMarker));
                assertThat(smsParser.extractTrType(), is(smsTest.mType));

                //тест извлечения получателя или счета назначения
                if (!smsTest.mPayee.isEmpty()) {
                    switch (smsTest.mType) {
                        case Transaction.TRANSACTION_TYPE_INCOME:
                        case Transaction.TRANSACTION_TYPE_EXPENSE:
                            Log.d(TAG, String.format("Payee %s with marker %s extracted (id = %d)", payee.getName(), smsTest.mPayee, payee.getID()));
                            assertThat(smsParser.extractPayee().getID(), is(payee.getID()));
                            break;
                        case Transaction.TRANSACTION_TYPE_TRANSFER:
                            Log.d(TAG, String.format("Dest Account %s with marker %s extracted (id = %d)", destAccount.getName(), smsTest.mPayee, destAccount.getID()));
                            assertThat(smsParser.extractDestAccount().getID(), is(destAccount.getID()));
                            break;
                    }
                }

                //тест извлечения суммы
                Log.d(TAG, String.format("Amount %s with currency %s extracted", new CabbageFormatter(cabbage1).format(smsTest.mAmount), cabbage1.getCode()));
                assertThat(smsParser.extractAmount(account), is(smsTest.mAmount));

                //тест извлечения баланса
                if (smsTest.mBalance != null) {
                    Log.d(TAG, String.format("Balance %s with currency %s extracted", new CabbageFormatter(cabbage1).format(smsTest.mBalance), cabbage1.getCode()));
                }
                assertThat(smsParser.extractBalance(), is(smsTest.mBalance));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    private void loadCSV() {
        final CSV csv = CSV
                .separator(',')  // delimiter of fields
                .quote('"')      // quote character
                .skipLines(1)
                .charset("UTF-8")
                .create();

        smsTests = new ArrayList<>();
//        csv.read("/storage/emulated/0/Fingen/sms_examples.csv", (i, strings) ->
//        csv.read(Environment.getExternalStorageDirectory().getPath() + "/Fingen/sms_examples.csv", new CSVReadProc() {
        InputStream stream = new ByteArrayInputStream(mSmsExamples.getBytes(StandardCharsets.UTF_8));
        csv.read(stream, new CSVReadProc() {
            @Override
            public void procRow(int i, String... strings) {
            /*Text	Account	TypeMarker	Type	Amount	CabbageAmount	Balance	CabbageBalance	Payee	DestAccount*/
                List<String> vls = Arrays.asList(strings);
                SmsTest smsTest = new SmsTest();
                smsTest.mText = vls.get(0).replaceAll("\"","").trim();
                smsTest.mAccountMarker = vls.get(1).replaceAll("\"","").trim();
                smsTest.mTypeMarker = vls.get(2).replaceAll("\"","").trim();
                smsTest.mType = Integer.valueOf(vls.get(3).replaceAll("\"","").trim());
                if (!vls.get(4).replaceAll("\"","").trim().isEmpty()) {
                    smsTest.mAmount = new BigDecimal(Double.valueOf(vls.get(4).replaceAll("\"","").trim()));
                }
                smsTest.mCabbageAmount = vls.get(5).replaceAll("\"","").trim();
                if (!vls.get(6).replaceAll("\"","").trim().isEmpty()) {
                    smsTest.mBalance = new BigDecimal(Double.valueOf(vls.get(6).replaceAll("\"","").trim()));
                }
                smsTest.mCabbageBalance = vls.get(7).replaceAll("\"","").trim();
                smsTest.mPayee = vls.get(8).replaceAll("\"","").trim();
                smsTest.mDestAccount = vls.get(9).replaceAll("\"","").trim();
                smsTest.mAmountPos = Integer.valueOf(vls.get(10).replaceAll("\"","").trim());
                smsTest.mBalancePos = Integer.valueOf(vls.get(11).replaceAll("\"","").trim());
                smsTests.add(smsTest);
            }
        });
    }

    private class SmsTest {
        String mText;
        String mAccountMarker;
        String mTypeMarker;
        int mType;
        BigDecimal mAmount;
        String mCabbageAmount;
        BigDecimal mBalance;
        String mCabbageBalance;
        String mPayee;
        String mDestAccount;
        int mAmountPos;
        int mBalancePos;
    }

    private static final String mSmsExamples =
"       \"Text                                                                                                                              \",\" Account         \",\" TypeMarker          \",\" Type \",\" Amount     \",\" CabbageAmount \",\" Balance    \",\" CabbageBalance \",\" Payee              \",\" DestAccount \",\" AmountPos \",\" BalancePos \" \n" +
/*1*/"  \"Pokupka 300.00 RUR schet **1234 Torgovaya tochka: BURGER KING Data 05/01/14 Balans: 1100.55 RUR                                   \",\" **1234          \",\" Pokupka             \",\" -1   \",\" 300.00     \",\" RUR           \",\" 1100.55    \",\" RUR            \",\" Torgovaya tochka   \",\"             \",\" 0         \",\" 1          \" \n" +
/*2*/"  \"Pokupka. Karta *6741. Summa 924.43 RUB. AUCHAN KHIMKI, KHIMKI. 25.07.2014 21:25. Dostupno 109784.70 RUB.                          \",\" *6741           \",\" Pokupka             \",\" -1   \",\" 924.43     \",\" RUB           \",\" 109784.70  \",\" RUB            \",\" AUCHAN             \",\"             \",\" 0         \",\" 1          \" \n" +
/*3*/"  \"Provedeno po schety 24/09/2014: + 129.76 RUR. Balans scheta karty *3027 na 25/09/2014: 1944.25 RUR. Raiffeisenbank                \",\" *3027           \",\" Provedeno           \",\" 1    \",\" 129.76     \",\" RUR           \",\" 1944.25    \",\" RUR            \",\" schety             \",\"             \",\" 0         \",\" 1          \" \n" +
/*4*/"  \"Provedeno po schety 24/09/2014: - 129.76 RUR. Balans scheta karty *3027 na 25/09/2014: - 1944.25 RUR. Raiffeisenbank              \",\" *3027           \",\" Provedeno           \",\" 1    \",\" 129.76     \",\" RUR           \",\" -1944.25   \",\" RUR            \",\" schety             \",\"             \",\" 0         \",\" 1          \" \n" +
/*5*/"  \"Spisanie po kreditnoy karte Summa: 59.00 RUB Torgovaya tochka: SEPTIMA NO Data: 05/02/15 Dostupniy limit: 290,506.86 RUB          \",\" kreditnoy       \",\" Spisanie            \",\" -1   \",\" 59.00      \",\" RUB           \",\" 290506.86  \",\" RUB            \",\" SEPTIMA            \",\"             \",\" 0         \",\" 1          \" \n" +
/*6*/"  \"VISAХХХ: 06.10.14 14:52 операция зачисления зарплаты на сумму 5155.56р. Баланс: 5328.80р.                                         \",\" VISAХХХ         \",\" операция зачисления \",\" 1    \",\" 5155.56    \",\" р             \",\" 5328.80    \",\" р              \",\" зарплаты           \",\"             \",\" 0         \",\" 1          \" \n" +
/*7*/"  \"VISAХХХ: 06.10.14 11:09 оплата услуг на сумму 110.00р. BEELINE (ХХХХХХХХХХ). Баланс: 173.24р.                                     \",\" VISAХХХ         \",\" оплата услуг        \",\" -1   \",\" 110.00     \",\" р             \",\" 173.24     \",\" р              \",\" BEELINE            \",\"             \",\" 0         \",\" 1          \" \n" +
/*8*/"  \"Balans vashego scheta #5368 popolnilsya 10/06/2015 na 4255.00 RUR. Dostupny ostatok 4213.28 RUR. Raiffeisenbank                   \",\" #5368           \",\" popolnilsya         \",\" 1    \",\" 4255.00    \",\" RUR           \",\" 4213.28    \",\" RUR            \",\"                    \",\"             \",\" 0         \",\" 1          \" \n" +
/*9*/"  \"Popolnenie scheta: 02.07.15 16:39,karta ***2480. summa:1503000.00 UZS, balance:8868329.63 UZS, my.uzcard.uz                       \",\" ***2480         \",\" Popolnenie          \",\" 1    \",\" 1503000.00 \",\" UZS           \",\" 8868329.63 \",\" UZS            \",\"                    \",\"             \",\" 0         \",\" 1          \" \n" +
/*10*/" \"05/12 08:21:35 пополн. счета *8926 на 28,399.94RUR дост. 35,495.48                                                                \",\" *8926           \",\" пополн.             \",\" 1    \",\" 28399.94   \",\" RUR           \",\"            \",\"                \",\"                    \",\"             \",\" 0         \",\" 1          \" \n" +
/*11*/" \"Popolnenie, karta:4970 summa:2000.00 RUR balans:21550.04 RUR (MSK 18:54:37 20.01.2015)                                            \",\" karta:4970      \",\" Popolnenie          \",\" 1    \",\" 2000.00    \",\" RUR           \",\" 21550.04   \",\" RUR            \",\"                    \",\"             \",\" 0         \",\" 1          \" \n" +
/*12*/" \"4*73 02/10/14 Spisanie 156.2UAH. Bal:1017.25UAH Za kreditnyi limit 2.9 na ostatok dolga. pb.ua1809                                \",\" 4*73            \",\" Spisanie            \",\" -1   \",\" 156.20     \",\" UAH           \",\" 1017.25    \",\" UAH            \",\"                    \",\"             \",\" 0         \",\" 1          \" \n" +
/*13*/" \"4*73 04:13 Oplata 10.00UAH (REG.PLATEZH). Bal:1320.45UAH mobilnoy svyazi klienta 2833500539 na nomer 380675125060                 \",\" 4*73            \",\" Oplata              \",\" -1   \",\" 10.00      \",\" UAH           \",\" 1320.45    \",\" UAH            \",\" REG.PLATEZH        \",\"             \",\" 0         \",\" 1          \" \n" +
/*14*/" \"4*73 19:16 Oplata 65.70UAH (Produkti). Bal:28.53UAH Oplata chastyami bez pereplat. Posmotret tochki: pb.ua/chast                  \",\" 4*73            \",\" Oplata              \",\" -1   \",\" 65.70      \",\" UAH           \",\" 28.53      \",\" UAH            \",\" Produkti           \",\"             \",\" 0         \",\" 1          \" \n" +
/*15*/" \"Karta 6...1192 oplata 999,71 RUR mesto GPN URAL 143-2 EKATERINBURG RUS 06/03/2015 Ostatok 18424,92 RUR                            \",\" Karta 6...1192  \",\" Oplata              \",\" -1   \",\" 999.71     \",\" RUR           \",\" 18424.92   \",\" RUR            \",\" GPN URAL           \",\"             \",\" 0         \",\" 1          \" \n" +
/*16*/" \"KARTA:4649*8246,27/03/15 19:02,OPLATA 79 250 BYR,SUPERMARKET ALMI, , MINSK,OSTATOK 31 806 BYR. Spravka: 5099999                   \",\" KARTA:4649*8246 \",\" Oplata              \",\" -1   \",\" 79250.00   \",\" BYR           \",\" 31806.00   \",\" BYR            \",\" SUPERMARKET ALMI   \",\"             \",\" 0         \",\" 1          \" \n" +
/*17*/" \"Pokupka, Summa:500,00 RUR, Karta:4..5604, Data:02.06.15, Inf:STRELKA, Kod:192579. Dostupno:22945.32 RUR                           \",\" Karta:4..5604   \",\" Pokupka             \",\" -1   \",\" 500.00     \",\" RUR           \",\" 22945.32   \",\" RUR            \",\" STRELKA            \",\"             \",\" 0         \",\" 1          \" \n" +
/*18*/" \"Pokupka. Karta *0221. Summa 10.00 RUB. VICTORIA-7, MOSKVA. 09.05.2015 17:09. Dostupno 381.11 RUB.                                 \",\" Karta *0221     \",\" Pokupka             \",\" -1   \",\" 10.00      \",\" RUB           \",\" 381.11     \",\" RUB            \",\" VICTORIA-7, MOSKVA \",\"             \",\" 0         \",\" 1          \" \n" +
/*19*/" \"VC: *0700 03.03.2015 07:17:14. Oplata tovarov/uslug v AZK 9 na summu 604.00 RUR vypolnena uspeshno.                               \",\" *0700           \",\" Oplata              \",\" -1   \",\" 604.00     \",\" RUR           \",\"            \",\" RUR            \",\" AZK 9              \",\"             \",\" 0         \",\" 1          \" \n" +
/*20*/" \"ЕCMC0173 25.05.15 21:10 выдача наличных 5000р ATM 574917 Баланс: 159.71р                                                          \",\" ЕCMC0173        \",\" выдача              \",\" 0    \",\" 5000.00    \",\" р             \",\" 159.71     \",\" р              \",\" наличных           \",\" наличных    \",\" 0         \",\" 1          \" \n" +
/*21*/" \"2014-12-16 11:44:08 Pokupka (IBANK.KHMB.RU) RRN 435082179616 Summa 5 000.00 RUR Dostupno 3 834.92 RUR Podderzhka +73467390700     \",\" (IBANK.KHMB.RU) \",\" Pokupka             \",\" -1   \",\" 5000.00    \",\" RUR           \",\" 3834.92    \",\" RUR            \",\" IBANK.KHMB.RU      \",\"             \",\" 0         \",\" 1          \" \n" +
/*22*/" \"Predavtorizacija, SPYTNIK SHOP, karta *1234, 22.12.14 16:52, 69.20 rub. Dostupno = 534.89 rub                                     \",\" karta *1234     \",\" Predavtorizacija    \",\" -1   \",\" 69.20      \",\" RUB           \",\" 534.89     \",\" RUB            \",\" SPYTNIK SHOP       \",\"             \",\" 0         \",\" 1          \" \n" +
/*23*/" \"14/12 10:43:14 АТМ 1,000.00RUR одобр. дост. 22,495.48 карта *5683 ATM 1620 BERDIGESTYAKH RU                                       \",\" карта *5683     \",\" одобр               \",\" -1   \",\" 1000.00    \",\" RUR           \",\"            \",\"                \",\" BERDIGESTYAKH RU   \",\"             \",\" 0         \",\" 1          \" \n" +
/*24*/" \"29/12 06:02:18 Счет *8926 блокировка на 60,000.00RUR дост. 19,278.81                                                              \",\" Счет *8926      \",\" блокировка          \",\" -1   \",\" 60000.00   \",\" RUR           \",\"            \",\"                \",\"                    \",\"             \",\" 0         \",\" 1          \" \n" +
/*25*/" \"4*83 12:04 Oplata 152.34RUR (PYATYU PYAT). Bal:69254.39RUR                                                                        \",\" 4*83            \",\" Oplata              \",\" -1   \",\" 152.34     \",\" RUR           \",\" 69254.39   \",\" RUR            \",\" PYATYU PYAT        \",\"             \",\" 0         \",\" 1          \" \n" +
/*26*/" \"Spisanie s karty MC *3053: -940.59 RUR 21/02/2015 02:34:03 MSK. Dostupno: 5840.44 RUR. Tel: 88007752424                           \",\" MC *3053        \",\" Spisanie            \",\" -1   \",\" 940.59     \",\" RUR           \",\" 5840.44    \",\" RUR            \",\"                    \",\"             \",\" 0         \",\" 1          \" \n" +
/*27*/" \"1*2345; Pokupka; Uspeshno; Summa: 123,45 RUR; Ostatok: 1234,56 RUR; RU/MOSCOW/U PALYCHA GDETO; 03.02.2016 10:07:34                \",\" 1*2345          \",\" Pokupka             \",\" -1   \",\" 123.45     \",\" RUR           \",\" 1234.56    \",\" RUR            \",\" U PALYCHA GDETO    \",\"             \",\" 0         \",\" 1          \" \n" +
/*28*/" \"irknet.ru (RUB 695.00); пароль: 643335. Не сообщайте этот пароль никому, в том числе сотруднику Банка.                            \",\"                 \",\"                     \",\" -1   \",\" 0.00       \",\" RUB           \",\"            \",\" RUB            \",\"                    \",\"             \",\" 0         \",\" 1          \" \n" +
/*29*/" \"Вход в Сбербанк Онлайн 05:35 16.02.16. Не вводите пароль для отмены или подтверждения операций, которые не совершали.             \",\"                 \",\"                     \",\" -1   \",\" 0.00       \",\"               \",\"            \",\"                \",\"                    \",\"             \",\" 0         \",\" 1          \" \n" +
/*30*/" \"Spisanie so scheta 408*01234 na summu 1,234.56 RUR, poluchatel platezha 408*56789; 12.02.2016 08:24:03                            \",\" 408*01234       \",\" poluchatel platezha \",\" 0    \",\" 1234.56    \",\" RUR           \",\"            \",\"                \",\" 408*56789          \",\" 408*56789   \",\" 0         \",\" 1          \" \n" +
/*31*/" \"Карта ***4114 123,00RUR Оплата картой APTEKA 24 CHASA; Успешно выполнено. Доступно:60209.62RUR                                    \",\" ***4114         \",\" Оплата картой       \",\" -1   \",\" 123.00     \",\" RUR           \",\" 60209.62   \",\" RUR            \",\"                    \",\"             \",\" 0         \",\" 1          \" \n" +
/*32*/" \"Pokupka 300.00 $ schet **1234 Torgovaya tochka: BURGER KING Data 05/01/14 Balans: 1100.55$                                        \",\" **1234          \",\" Pokupka             \",\" -1   \",\" 300.00     \",\" $             \",\" 1100.55    \",\" $              \",\" Torgovaya tochka   \",\"             \",\" 0         \",\" 1          \" \n" +
/*33*/" \"Telecard; Card0700; Snyatie nalichnih; Summa 1900 RUR; 03.08.16 10:37:28; GAZPROMBANK; dostupno 100500 RUR                        \",\" Card0700        \",\" Snyatie             \",\" 0    \",\" 1900.00    \",\" RUR           \",\" 100500.00  \",\" RUR            \",\"                    \",\" nalichnih   \",\" 0         \",\" 1          \" \n" +
/*34*/" \"Karta *5293: Oplata 12.60 RUR; YM*YandexTaxi; 23.07.2016 12:49, dostupno 61991.56 RUR (v tom chisle kred. 47500.00 RUR). VTB24    \",\" *5293           \",\" Oplata              \",\" -1   \",\" 12.60      \",\" RUR           \",\" 61991.56   \",\" RUR            \",\" YandexTaxi         \",\"             \",\" 0         \",\" 1          \" \n" +
/*35*/" \"14:22 12/07/16 Покупка Сумма:10.10 руб по карте *4114 W.QIWI.RU, WWW.QIWI.COM, Баланс до jht: 3 086.54 руб Доступно: 3 076.44 руб \",\" *4114           \",\" Покупка             \",\" -1   \",\" 10.10      \",\" руб           \",\" 3076.44    \",\" руб            \",\" WWW.QIWI.COM       \",\"             \",\" 0         \",\" 2          \" \n" +
/*36*/" \"18.11.2016 16:35:16 VISA Classic/1385 4.17 USD WWW.ALIEXPRESS.COM dostupna suma 79.56 USD                                         \",\" VISA            \",\" ALIEXPRESS          \",\" -1   \",\" 4.17       \",\" USD           \",\" 79.56      \",\" USD            \",\" ALIEXPRESS         \",\"             \",\" 0         \",\" 1          \" \n" +
/*37*/" \"VISA3680 30.09.16 оплата Мобильного банка за 30/09/2016-30/10/2016 60р Баланс: 14619.33р                                          \",\" VISA3680        \",\" оплата              \",\" -1   \",\" 60         \",\" р             \",\" 14619.33   \",\" р              \",\" Мобильного банка   \",\"             \",\" 0         \",\" 1          \" \n";

}
