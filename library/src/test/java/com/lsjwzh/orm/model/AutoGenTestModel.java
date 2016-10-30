package com.lsjwzh.orm.model;


import java.util.Date;

import com.lsjwzh.orm.Model;
import com.lsjwzh.orm.annotations.AutoGen;
import com.lsjwzh.orm.annotations.AutoIncrement;
import com.lsjwzh.orm.annotations.Key;
import com.lsjwzh.orm.annotations.Table;

@Table
@AutoGen
public class AutoGenTestModel extends Model {

    @Key
    @AutoIncrement
    public long id;
    public String title;
    public Date createdAt;

    public boolean created;
    public boolean saved;
    public boolean deleted;

}