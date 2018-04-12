package com.yoshione.fingen.filters;

import android.content.Context;

import com.yoshione.fingen.dao.AccountsDAO;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

/**
 * Created by Leonid on 06.12.2016.
 * Добавляет в список фильтров фильтр исключающий отображение закрытых счетов.
 * Нужен для того, чтобы закрытые счета не добавлялись в итоговые суммы и не отображались транзакции по ним
 */

public class FilterListHelper {
    private List<AbstractFilter> mFilters;
    private String mSearchString;
    private Context mContext;

    public FilterListHelper(List<AbstractFilter> filters, String searchString, Context context) {
        mFilters = filters;
        mSearchString = searchString;
        mContext = context;
    }

    public String getSearchString() {
        return mSearchString;
    }

    public List<AbstractFilter> getFilters() {
        //Получаем список ID закрытых счетов
        HashSet<Long> closedAccountsIDs = AccountsDAO.getInstance(mContext).getClosedAccountsIDs();

        //Создаем новый фильтр
        AccountFilter filter = new AccountFilter(new Random().nextInt());

        //добавляем в него список ID
        filter.getIDsSet().addAll(closedAccountsIDs);

        //Указываем, что фильтр инвертировано (НЕ)
        filter.setInverted(true);

        //Добавляем новый фильтр к списку фильтров и возвращаем общий список
        List<AbstractFilter> filters = new ArrayList<>(mFilters);
        filters.add(filter);
        return filters;
    }
}
