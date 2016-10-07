package com.autumncode.logs.model;

import lombok.Data;

import javax.persistence.*;
import java.util.Date;

@Entity
@Data
@Inheritance(strategy = InheritanceType.JOINED)
public class Log {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    Long id;
    @Temporal(value = TemporalType.TIMESTAMP)
    Date createdAt;
    String message;

}
