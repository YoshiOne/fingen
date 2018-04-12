package com.yoshione.fingen.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.util.Pair;

import com.yoshione.fingen.dao.AccountsDAO;
import com.yoshione.fingen.dao.PayeesDAO;
import com.yoshione.fingen.dao.SmsMarkersDAO;
import com.yoshione.fingen.managers.SmsManager;
import com.yoshione.fingen.managers.TransactionManager;
import com.yoshione.fingen.model.Account;
import com.yoshione.fingen.model.Payee;
import com.yoshione.fingen.model.Sender;
import com.yoshione.fingen.model.Sms;
import com.yoshione.fingen.model.Transaction;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by slv on 29.10.2015.
 *
 */
public class SmsParser {

    public final static int MARKER_TYPE_ACCOUNT = 0;
    public final static int MARKER_TYPE_CABBAGE = 1;
    public final static int MARKER_TYPE_TRTYPE = 2;
    public final static int MARKER_TYPE_PAYEE = 3;
    public final static int MARKER_TYPE_IGNORE = 4;
//    public final static int MARKER_TYPE_PHONENO = 5;
    public final static int MARKER_TYPE_DESTACCOUNT = 6;
    private static final String TAG = "SmsParser";
    public Pair<Integer, Integer> mAccountBorders = new Pair<>(0,0);
    public Pair<Integer, Integer> mTrTypeBorders = new Pair<>(0,0);
    public Pair<Integer, Integer> mPayeeBorders = new Pair<>(0,0);
    public Pair<Integer, Integer> mDestAccountBorders = new Pair<>(0,0);
    public Pair<Integer, Integer> mAmountBorders = new Pair<>(0,0);
    public Pair<Integer, Integer> mCabbageAmountBorders = new Pair<>(0,0);
    public Pair<Integer, Integer> mCabbageBalanceBorders = new Pair<>(0,0);
    public Pair<Integer, Integer> mBalanceBorders = new Pair<>(0,0);
    public String mCurrentPayeeMarker = "";
    public String mCurrentDestAccountMarker = "";
    public String mCurrentAccountMarker = "";
    public String mCurrentTrTypeMarker = "";
    private final Sms mSms;
    private final Context mContext;
    private final List<Pair<Long, List<String>>> mAccountMarkers;
    private final List<Pair<Long, List<String>>> mCabbageMarkers;
    private final List<Pair<Long, List<String>>> mPayeesMarkers;
    private final List<Pair<Long, List<String>>> mDestAccountMarkers;
    private final List<Pair<Integer, List<String>>> mTrTypeMarkers;
    private Sender mSender;
    private List<String> mIgnoreMarkers;
//    public String mCurrentCabbageMarker = "";


    public SmsParser(Sms mSms, Context context){
        this.mSms = mSms;
        this.mSender = SmsManager.getSender(mSms, context);
        this.mContext = context.getApplicationContext();

        SmsMarkersDAO smsMarkersDAO = SmsMarkersDAO.getInstance(mContext);

        mAccountMarkers = new LinkedList<>();
        mCabbageMarkers = new LinkedList<>();
        mIgnoreMarkers = new ArrayList<>();
        mTrTypeMarkers = new LinkedList<>();
        mPayeesMarkers = new LinkedList<>();
        mDestAccountMarkers = new LinkedList<>();

        List<String> objects;
        List<String> patterns;

        objects = smsMarkersDAO.getAllObjectsByType(MARKER_TYPE_ACCOUNT);
        long accountId;
        for (String object:objects){
            try {
                accountId = Long.valueOf(object);
            } catch (NumberFormatException e) {
                accountId = -1;
            }
            if (accountId >= 0) {
                patterns = smsMarkersDAO.getAllMarkersByObject(MARKER_TYPE_ACCOUNT, object);
                mAccountMarkers.add(new Pair<>(accountId, patterns));
            }
        }

        objects = smsMarkersDAO.getAllObjectsByType(MARKER_TYPE_CABBAGE);
        long cabbageId;
        for (String object:objects){
            try {
                cabbageId = Long.valueOf(object);
            } catch (NumberFormatException e) {
                cabbageId = -1;
            }
            if (cabbageId >= 0) {
                patterns = smsMarkersDAO.getAllMarkersByObject(MARKER_TYPE_CABBAGE, object);
                mCabbageMarkers.add(new Pair<>(cabbageId, patterns));
            }
        }

        objects = smsMarkersDAO.getAllObjectsByType(MARKER_TYPE_TRTYPE);
        for (String object:objects){
            patterns = smsMarkersDAO.getAllMarkersByObject(MARKER_TYPE_TRTYPE, object);
            mTrTypeMarkers.add(new Pair<>(Integer.valueOf(object), patterns));
        }

        long payeeId;
        objects = smsMarkersDAO.getAllObjectsByType(MARKER_TYPE_PAYEE);
        for (String object:objects){
            try {
                payeeId = Long.valueOf(object);
            } catch (NumberFormatException e) {
                payeeId = -1;
            }
            if (payeeId >= 0) {
                patterns = smsMarkersDAO.getAllMarkersByObject(MARKER_TYPE_PAYEE, object);
                mPayeesMarkers.add(new Pair<>(payeeId, patterns));
            }
        }

        objects = smsMarkersDAO.getAllObjectsByType(MARKER_TYPE_DESTACCOUNT);
        long destAccountId;
        for (String object:objects){
            try {
                destAccountId = Long.valueOf(object);
            } catch (NumberFormatException e) {
                destAccountId = -1;
            }
            if (destAccountId >= 0) {
                patterns = smsMarkersDAO.getAllMarkersByObject(MARKER_TYPE_DESTACCOUNT, object);
                mDestAccountMarkers.add(new Pair<>(destAccountId, patterns));
            }
        }

        mIgnoreMarkers = smsMarkersDAO.getAllPatternsByType(MARKER_TYPE_IGNORE);

        extractTransaction();
    }

    public Transaction extractTransaction(){
        Transaction transaction = new Transaction(PrefUtils.getDefDepID(mContext));

        transaction.setDateTime(mSms.getmDateTime());
        Account account = AccountsDAO.getInstance(mContext).getAccountByID(extractAccount().getID());
        transaction.setAccountID(account.getID());
        extractBalance();

        switch (extractTrType()){
            case Transaction.TRANSACTION_TYPE_INCOME:{
                transaction.setPayeeID(extractPayee().getID());
                transaction.setCategoryID(TransactionManager.getPayee(transaction, mContext).getDefCategoryID());
                transaction.setAmount(extractAmount(account),Transaction.TRANSACTION_TYPE_INCOME);
                break;
            }
            case Transaction.TRANSACTION_TYPE_EXPENSE:{
                transaction.setPayeeID(extractPayee().getID());
                transaction.setCategoryID(TransactionManager.getPayee(transaction, mContext).getDefCategoryID());
                transaction.setAmount(extractAmount(account),Transaction.TRANSACTION_TYPE_EXPENSE);
                break;
            }
            case Transaction.TRANSACTION_TYPE_TRANSFER:{
                transaction.setAmount(extractAmount(account),Transaction.TRANSACTION_TYPE_TRANSFER);
                transaction.setDestAccountID(extractDestAccount().getID());
                break;
            }
            default:{

            }
        }

        return transaction;
    }

    /**
     * Метод проверяет пригодность смс к парсингу, проверяя, есть ли номер отправителя в списке и нет ли маркеров игнора
     * @return тру - можно парсить, фалс - нельзя
     */
    public boolean goodToParse(){
        Boolean marker = true;

        if (mSms.getmBody().split("\\s+").length <= 1) {
            return false;
        }

        for (String str : mIgnoreMarkers) {
            if (mSms.getmBody().toLowerCase().contains(str.toLowerCase())) {
                marker = false;
                break;
            }
        }

        return marker;
    }

    public BigDecimal checkBalance(Account account) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        BigDecimal result = BigDecimal.ZERO;
        if (!preferences.getBoolean("check_actual_balance_in_sms", true)) return result;
        BigDecimal actualBalance = extractBalance();
        if (actualBalance == null) return result;

        BigDecimal currentBalance = account.getCurrentBalance();
        if (mSender.isAddCreditLimitToBalance()) {
            currentBalance = account.getCreditLimit().abs().add(currentBalance);
        }
        String error = preferences.getString("balance_compare_error", "1");
        BigDecimal balanceCompareError;
        try {
            balanceCompareError = new BigDecimal(Double.valueOf(error));
        } catch (NumberFormatException e) {
            balanceCompareError = BigDecimal.ONE;
        }

        result = actualBalance.subtract(currentBalance);

        if (result.abs().compareTo(balanceCompareError.abs()) > 0) {
            return result;
        } else {
            return BigDecimal.ZERO;
        }
    }

    /*public Account extractDestAccount() {
        Account account = new Account();
        Pattern pattern;
        Matcher matcher;
        AccountsDAO accountsDAO = AccountsDAO.getInstance(mContext);
        for (int i = 0; i < mDestAccountMarkers.size(); i++) {
            for (int j = 0; j < mDestAccountMarkers.get(i).second.size(); j++) {
                pattern = Pattern.compile(mDestAccountMarkers.get(i).second.get(j).toLowerCase());
                matcher = pattern.matcher(mSms.getmBody().toLowerCase());
                if (matcher.find()) {
                    account = accountsDAO.getAccountByID(mDestAccountMarkers.get(i).first);
                    mDestAccountBorders = new Pair<>(matcher.start(), matcher.end());
                    mCurrentDestAccountMarker = mDestAccountMarkers.get(i).second.get(j);
                    break;
                }
            }
        }
        return account;
    }*/

    /**
     * Удаляет форматирование со строки, представляющей число
     * Удаляем все точки и запятые кроме последней
     * Если после последней > 2 цифр, значит это разделитель тысяч, удаляем и ее
     * @param s - число
     * @return - число без форматирования, пригодное для преобразования в double
     */
    private static String unformatNumber(String s) {
        //грохаем все символы кроме цифр, минуса, точки и запятой. Затем заменяем запятые на точки
        String s1 = s.replaceAll("[^\\d.,-]", "").replaceAll(",", ".");

        /*Ищем дробную часть (одна или 2 цифры после точки в конце строки)*/
        Pattern pattern = Pattern.compile("([\\.,]\\d{1,2})$");
        Matcher matcher = pattern.matcher(s1);
        String frac = "";
        while (matcher.find()) {
            //если нашли сохраняем ее
            frac = matcher.group().replaceAll("\\.", ".");
            //и удаляем из исходного числа
            s1 = s1.substring(0,s1.length()-frac.length());
        }

        //считаем, что остались только разделители тысяч и грохаем их
        s1 = s1.replaceAll("\\.", "");

        //возвращаем конкатенацию целой и дробной частей
        return s1.concat(frac);
    }

    private List<AmountMatch> getAmountMatches() {
        List<AmountMatch> matches = new ArrayList<>();
        String currentCabbageMarker;
        BigDecimal amount;
        long cabbageId;
        int firstCabbage;
        int secondCabbage;
        int firstAmount;
        int secondAmount;
        Pattern pattern;
        Matcher matcher;
        AmountMatch amountMatch;
        String body = mSms.getmBody().toLowerCase().replaceAll("\\d{2}\\/\\d{2}\\/\\d{4}", "dd/mm/yyyy");
        for (int i = 0; i < mCabbageMarkers.size(); i++) {
            for (int j = 0; j < mCabbageMarkers.get(i).second.size(); j++) {
                /*
                (?<![\*|\/\d]) - отбрасываем первую группу цифр, если она начинается со звездочки или со слэша
                -? опциональный минус
                \s? - опциональный пробел
                (\d+(,|.| ))+ одна или несколько цифр, с символом в конце (зпт, тчк, пробел), и так несколько раз
                \d+ должно заканчиваться цифрой
                \s? потом может быть пробел
                в конце идет маркер валюты
                (?=\P{L}|$) после маркера валюты должен быть любой символ НЕ буква (\P{L}) или (|) конец строки ($). Этот символ не включается в результат (?=)
                */
                pattern = Pattern.compile("(?<![\\*|\\/\\d])-?\\s?(\\d+(,|.| ))*\\d+\\s?" + mCabbageMarkers.get(i).second.get(j).toLowerCase() + "(?=\\P{L}|$)");
                matcher = pattern.matcher(body);
                while (matcher.find()) {
                    try {
                        amount = new BigDecimal(Double.parseDouble(unformatNumber(matcher.group())));
                    } catch (NumberFormatException e) {
                        amount = BigDecimal.ZERO;
                    }
                    cabbageId = mCabbageMarkers.get(i).first;
                    firstAmount = matcher.start();
                    secondAmount = matcher.end();
                    currentCabbageMarker = mCabbageMarkers.get(i).second.get(j);
                    firstCabbage = mSms.getmBody().indexOf(currentCabbageMarker, matcher.start());
                    secondCabbage = firstCabbage + currentCabbageMarker.length();
                    amountMatch = new AmountMatch();
                    amountMatch.setmAmount(amount);
                    amountMatch.setmFirstCabbage(firstCabbage);
                    amountMatch.setmSecondCabbage(secondCabbage);
                    amountMatch.setmFirstAmount(firstAmount);
                    amountMatch.setmSecondAmount(secondAmount);
                    amountMatch.setmCabbageId(cabbageId);
                    matches.add(amountMatch);
                }
            }
        }
        return matches;
    }

    public BigDecimal extractAmount(Account account) {
        List<AmountMatch> matches = getAmountMatches();

        AmountMatch amountMatch;
        BigDecimal result;
        if (matches.size() > mSender.getAmountPos()) {
            Collections.sort(matches);
            amountMatch = matches.get(mSender.getAmountPos());
            mCabbageAmountBorders = checkBorders(new Pair<>(amountMatch.getmFirstCabbage(), amountMatch.getmSecondCabbage()));
            mAmountBorders = checkBorders(new Pair<>(amountMatch.getmFirstAmount(), amountMatch.getmSecondAmount()));
            if ((amountMatch.getmCabbageId() >= 0) & (account.getCabbageId() >= 0)) {
                if (account.getCabbageId() != amountMatch.getmCabbageId()) {
                    result = checkBalance(account);
                } else {
                    result = amountMatch.getmAmount();
                }
            } else {
                result = amountMatch.getmAmount();
            }
        } else {
            result = BigDecimal.ZERO;
        }
        return result.abs();
    }

    public BigDecimal extractBalance() {
        if (mSender == null) {
            return BigDecimal.ZERO;
        }
        List<AmountMatch> matches = getAmountMatches();

        AmountMatch amountMatch;

        if (matches.size() > mSender.getBalancePos()) {
            Collections.sort(matches);
            amountMatch = matches.get(mSender.getBalancePos());
            mCabbageBalanceBorders = checkBorders(new Pair<>(amountMatch.getmFirstCabbage(), amountMatch.getmSecondCabbage()));
            mBalanceBorders = checkBorders(new Pair<>(amountMatch.getmFirstAmount(), amountMatch.getmSecondAmount()));
            return amountMatch.getmAmount();
        } else {
            return null;
        }
    }

    public Account extractAccount(){
        Account account = new Account();

        AccountsDAO accountsDAO = AccountsDAO.getInstance(mContext);
        for (int i = 0; i < mAccountMarkers.size();i++){
            for (int j = 0; j < mAccountMarkers.get(i).second.size();j++){
                if (mSms.getmBody().toLowerCase().contains(mAccountMarkers.get(i).second.get(j).toLowerCase())){
                    account = accountsDAO.getAccountByID(mAccountMarkers.get(i).first);
                    int first = mSms.getmBody().toLowerCase().indexOf(mAccountMarkers.get(i).second.get(j).toLowerCase());
                    int second = first + mAccountMarkers.get(i).second.get(j).length();
                    mAccountBorders = checkBorders(new Pair<>(first,second));
                    mCurrentAccountMarker = mAccountMarkers.get(i).second.get(j);
                    break;
                }
            }
        }

        return account;
    }

    public Account extractDestAccount(){
        Account account = new Account();

        AccountsDAO accountsDAO = AccountsDAO.getInstance(mContext);
        for (int i = 0; i < mDestAccountMarkers.size();i++){
            for (int j = 0; j < mDestAccountMarkers.get(i).second.size();j++){
                if (mSms.getmBody().toLowerCase().contains(mDestAccountMarkers.get(i).second.get(j).toLowerCase())){
                    account = accountsDAO.getAccountByID(mDestAccountMarkers.get(i).first);
                    int first = mSms.getmBody().toLowerCase().indexOf(mDestAccountMarkers.get(i).second.get(j).toLowerCase());
                    int second = first + mDestAccountMarkers.get(i).second.get(j).length();
                    mDestAccountBorders = checkBorders(new Pair<>(first,second));
                    mCurrentDestAccountMarker = mDestAccountMarkers.get(i).second.get(j);
                    break;
                }
            }
        }

        return account;
    }

    public int extractTrType(){
        int result = Transaction.TRANSACTION_TYPE_EXPENSE;
        for (int i = 0; i < mTrTypeMarkers.size();i++){
            for (int j = 0;j < mTrTypeMarkers.get(i).second.size();j++){
                if (mSms.getmBody().toLowerCase().contains(mTrTypeMarkers.get(i).second.get(j).toLowerCase())){
                    result = mTrTypeMarkers.get(i).first;
                    int first = mSms.getmBody().toLowerCase().indexOf(mTrTypeMarkers.get(i).second.get(j).toLowerCase());
                    int second = first + mTrTypeMarkers.get(i).second.get(j).length();
                    mTrTypeBorders = checkBorders(new Pair<>(first,second));
                    mCurrentTrTypeMarker = mTrTypeMarkers.get(i).second.get(j);
                    break;
                }
            }
        }
        return result;
    }

    public Payee extractPayee(){
        Payee payee = new Payee();
        PayeesDAO payeesDAO = PayeesDAO.getInstance(mContext);
        for (int i = 0; i < mPayeesMarkers.size();i++){
            for (int j = 0;j < mPayeesMarkers.get(i).second.size();j++){
                if (mSms.getmBody().toLowerCase().contains(mPayeesMarkers.get(i).second.get(j).toLowerCase())){
                    payee = payeesDAO.getPayeeByID(mPayeesMarkers.get(i).first);
                    int first = mSms.getmBody().toLowerCase().indexOf(mPayeesMarkers.get(i).second.get(j).toLowerCase());
                    int second = first + mPayeesMarkers.get(i).second.get(j).length();
                    mPayeeBorders = checkBorders(new Pair<>(first,second));
                    mCurrentPayeeMarker = mPayeesMarkers.get(i).second.get(j);
                    break;
                }
            }
        }
        return payee;
    }

    private class AmountMatch implements Comparable<AmountMatch> {
        private BigDecimal mAmount;
        private int mFirstCabbage;
        private int mSecondCabbage;
        private int mFirstAmount;
        private int mSecondAmount;
        private long mCabbageId;

        long getmCabbageId() {
            return mCabbageId;
        }

        void setmCabbageId(long mCabbageId) {
            this.mCabbageId = mCabbageId;
        }

        int getmFirstAmount() {
            return mFirstAmount;
        }

        void setmFirstAmount(int mFirstAmount) {
            this.mFirstAmount = mFirstAmount;
        }

        int getmSecondAmount() {
            return mSecondAmount;
        }

        void setmSecondAmount(int mSecondAmount) {
            this.mSecondAmount = mSecondAmount;
        }

        public BigDecimal getmAmount() {
            return mAmount;
        }

        public void setmAmount(BigDecimal mAmount) {
            this.mAmount = mAmount;
        }

        int getmFirstCabbage() {
            return mFirstCabbage;
        }

        void setmFirstCabbage(int mFirstCabbage) {
            this.mFirstCabbage = mFirstCabbage;
        }

        int getmSecondCabbage() {
            return mSecondCabbage;
        }

        void setmSecondCabbage(int mSecondCabbage) {
            this.mSecondCabbage = mSecondCabbage;
        }

        @Override
        public int compareTo(@NonNull AmountMatch another) {
            return mSecondCabbage - another.mSecondCabbage;
        }
    }

    private Pair<Integer, Integer> checkBorders(Pair<Integer, Integer> pair) {
        int first = 0;
        int second = 0;
        if (pair.first > 0) {
            first = pair.first;
        }
        if (pair.second > 0) {
            second = pair.second;
        }

        return new Pair<>(first, second);
    }
}
