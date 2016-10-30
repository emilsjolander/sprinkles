package com.lsjwzh.orm.model;


import com.lsjwzh.orm.Model;
import com.lsjwzh.orm.annotations.AutoGen;
import com.lsjwzh.orm.annotations.AutoIncrement;
import com.lsjwzh.orm.annotations.Key;
import com.lsjwzh.orm.annotations.ManyToOne;
import com.lsjwzh.orm.annotations.Table;

@Table
@AutoGen
public class Email extends Model {

    @Key
    @AutoIncrement
    public long id;

    public String address;

    @ManyToOne(manyColumn = "owner_id",oneColumn = "id")
    public Person owner;

}