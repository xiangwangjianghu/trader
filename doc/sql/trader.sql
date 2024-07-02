CREATE TABLE public.t_order (
                                id int8 NOT NULL,
                                uid int8 NOT NULL,
                                code int4 NOT NULL,
                                direction int4 NOT NULL,
                                "type" int4 NOT NULL,
                                price numeric(20, 2) NOT NULL,
                                count numeric(20, 2) NOT NULL,
                                status int4 NOT NULL,
                                create_time timestamp DEFAULT now() NOT NULL,
                                update_time timestamp NULL,
                                CONSTRAINT t_order_pkey PRIMARY KEY (id)
);

-----------------------------------------------------------------------------

CREATE TABLE public.t_position (
                                   id int8 NOT NULL,
                                   uid int8 NOT NULL,
                                   code int4 NOT NULL,
                                   "cost" numeric(20, 2) NOT NULL,
                                   count numeric(20, 2) NOT NULL,
                                   create_time timestamp DEFAULT now() NOT NULL,
                                   update_time timestamp NULL,
                                   CONSTRAINT t_posi_pkey PRIMARY KEY (id)
);

-----------------------------------------------------------------------------

CREATE TABLE public.t_stock (
                                code int4 NOT NULL,
                                "name" varchar(20) NOT NULL,
                                abbr_name varchar(10) NOT NULL,
                                status int4 NOT NULL,
                                create_time timestamp DEFAULT now() NOT NULL,
                                update_time timestamp NULL,
                                CONSTRAINT t_stock_pkey PRIMARY KEY (code)
);

-----------------------------------------------------------------------------

CREATE TABLE public.t_trade (
                                id int8 NOT NULL,
                                uid int8 NOT NULL,
                                code int4 NOT NULL,
                                direction int4 NOT NULL,
                                price numeric(20, 2) NOT NULL,
                                count numeric(20, 2) NOT NULL,
                                "oid" int8 NOT NULL,
                                update_time timestamp NULL,
                                create_time timestamp DEFAULT now() NOT NULL
);

-----------------------------------------------------------------------------

CREATE TABLE public.t_user (
                               id int8 NOT NULL,
                               username varchar(64) NOT NULL,
                               "password" varchar(64) DEFAULT NULL::character varying NOT NULL,
                               balance numeric(20, 2) NULL,
                               create_time timestamp DEFAULT now() NULL,
                               update_time timestamp NULL,
                               CONSTRAINT t_user_pkey PRIMARY KEY (id),
                               CONSTRAINT t_user_uid_key UNIQUE (username)
);