package ru.ifmo.ctddev.slyusarenko.bank;

import java.util.concurrent.ConcurrentHashMap;

public class LocalPersonImpl implements LocalPerson {

    private ConcurrentHashMap<String, Account> accounts;
    private String name;
    private String surname;
    private String passportNumber;

    public LocalPersonImpl(Person p) {
        try {
            name = p.getName();
            surname = p.getSurname();
            passportNumber = p.getPassportNumber();
            accounts = p.getAccounts();
        } catch (Exception e) {
            name = "";
            surname = "";
            passportNumber = "";
            accounts = new ConcurrentHashMap<>();
        }
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getSurname() {
        return surname;
    }

    @Override
    public String getPassportNumber() {
        return passportNumber;
    }

    @Override
    public ConcurrentHashMap<String, Account> getAccounts() {
        return accounts;
    }

    @Override
    public int setAmount(String accountId, int amount) {
        Account account = getAccount(accountId);
        account.setAmount(amount);
        accounts.put(accountId, account);
        return account.getAmount();
    }

    @Override
    public String showMoney(String accountId) {
        Account account = getAccount(accountId);
        return ("Name: " + name + "\nSurname: " + surname + "\nPassport number: " + passportNumber + "\nID: " + account.getId() + "\nMoney: " + account.getAmount());
    }

    @Override
    public int getMoney(String accountId) {
        return getAccount(accountId).getAmount();
    }

    private Account getAccount(String accountId) {
        if (!accounts.containsKey(accountId)) {
            accounts.put(accountId, new AccountImpl(accountId));
        }
        return accounts.get(accountId);
    }
}
