create table category (
    id varchar(32),
    typename varchar(32),
    typevalue varchar(4),
    primary key (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE utf8_bin;

create table modelcategory (
    modeId varchar(32),
    typeId varchar(32),
    primary key (modeId,typeId)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE utf8_bin;

create table templateform (
    id varchar(32),
    name varchar(32),
    url varchar(32),
    templatetype varchar(32),
    primary key (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE utf8_bin;

create table templatecategory (
    templateId varchar(32),
    modelId varchar(32),
    primary key (templateId,modelId)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE utf8_bin;
    