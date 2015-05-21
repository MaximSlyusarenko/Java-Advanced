package ru.ifmo.ctddev.slyusarenko.bank;

import java.io.Serializable;
import java.util.concurrent.ConcurrentHashMap;

public interface LocalPerson extends Serializable, Person {

    public String getName();
    public String getSurname();
    public String getPassportNumber();
    public ConcurrentHashMap<String, Account> getAccounts();
    public int setAmount(String accountId, int amount);
    public String showMoney(String key);
    public int getMoney(String key);
}
