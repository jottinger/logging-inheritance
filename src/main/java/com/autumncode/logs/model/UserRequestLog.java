package com.autumncode.logs.model;

import lombok.Data;

import javax.persistence.Entity;

@Entity
@Data
public class UserRequestLog extends Log{
    String request;
}
