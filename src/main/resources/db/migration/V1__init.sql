CREATE  TABLE project_asset (
                                id                   serial        NOT NULL  ,
                                project_guid         varchar(100)  NOT NULL  ,
                                asset_guid           varchar(100)  NOT NULL  ,
                                CONSTRAINT pk_project_asset PRIMARY KEY ( id )
);