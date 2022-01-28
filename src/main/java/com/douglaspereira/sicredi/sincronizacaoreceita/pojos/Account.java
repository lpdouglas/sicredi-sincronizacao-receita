package com.douglaspereira.sicredi.sincronizacaoreceita.pojos;

import com.douglaspereira.sicredi.sincronizacaoreceita.enums.StatusContaEnum;
import com.douglaspereira.sicredi.sincronizacaoreceita.enums.SyncResultEnum;

public class Account {
    private String agency;
    private String account;
    private Double balance;
    private StatusContaEnum status;
    private SyncResultEnum result;

    public Account(String agency, String account, Double balance, StatusContaEnum status) {
        this.agency = agency;
        this.account = account;
        this.balance = balance;
        this.status = status;
    }

    public String getAgency() {
        return agency;
    }

    public String getAccount() {
        return account;
    }

    public Double getBalance() {
        return balance;
    }

    public StatusContaEnum getStatus() {
        return status;
    }

    public SyncResultEnum getResult() {
        return result;
    }

    public void setResult(SyncResultEnum result) {
        this.result = result;
    }
}
