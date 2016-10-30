package com.lsjwzh.orm.model;


import com.lsjwzh.orm.Model;
import com.lsjwzh.orm.LazyModelList;
import com.lsjwzh.orm.annotations.AutoGen;
import com.lsjwzh.orm.annotations.AutoIncrement;
import com.lsjwzh.orm.annotations.Key;
import com.lsjwzh.orm.annotations.OneToMany;
import com.lsjwzh.orm.annotations.Table;

@Table
@AutoGen
public class Company extends Model {

    @Key
    @AutoIncrement
    public long id;
    public String name;

    @OneToMany(manyColumn = "company_id",oneColumn = "id")
    public LazyModelList<Person> Staffs;

}