package ru.ifmo.ctddev.slyusarenko.bank;

import java.util.concurrent.ConcurrentHashMap;

public interface Person {

    public String getName() throws Exception;
    public String getSurname() throws Exception;
    public String getPassportNumber() throws Exception;
    public ConcurrentHashMap<String, Account> getAccounts() throws Exception;
    public int setAmount(String accountId, int amount) throws Exception;
    public String showMoney(String key) throws Exception;
    public int getMoney(String key) throws Exception;
}
