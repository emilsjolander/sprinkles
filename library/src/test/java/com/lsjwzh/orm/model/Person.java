package com.lsjwzh.orm.model;


import com.lsjwzh.orm.LazyModel;
import com.lsjwzh.orm.Model;
import com.lsjwzh.orm.ModelList;
import com.lsjwzh.orm.annotations.AutoGen;
import com.lsjwzh.orm.annotations.AutoIncrement;
import com.lsjwzh.orm.annotations.Key;
import com.lsjwzh.orm.annotations.ManyToOne;
import com.lsjwzh.orm.annotations.OneToMany;
import com.lsjwzh.orm.annotations.Table;

@Table
@AutoGen
public class Person extends Model {

    @Key
    @AutoIncrement
    public long id;
    public String name;
    public long company_id;


    @OneToMany(manyColumn = "owner_id",oneColumn = "id")
    public ModelList<Email> emails = new ModelList<>();

    @ManyToOne(manyColumn = "company_id",oneColumn = "id")
    public LazyModel<Company> company;

}