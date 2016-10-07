package com.autumncode.logs.model;

import lombok.Data;

import javax.persistence.Entity;

@Data
@Entity
public class TransactionLog extends Log {
    String statement;
}
