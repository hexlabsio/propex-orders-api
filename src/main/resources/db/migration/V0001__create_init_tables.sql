CREATE TABLE public."order"
(
    identifier character varying(36) COLLATE pg_catalog."default" NOT NULL,
    "order" character varying(50) COLLATE pg_catalog."default" NOT NULL,
    "dateTime" timestamp without time zone NOT NULL,
    serial character varying(50) COLLATE pg_catalog."default",
    model character varying(50) COLLATE pg_catalog."default",
    CONSTRAINT order_pkey PRIMARY KEY (identifier)
);

CREATE TABLE public.product
(
    identifier character varying(36) COLLATE pg_catalog."default" NOT NULL,
    "order" character varying(36) COLLATE pg_catalog."default" NOT NULL,
    serial character varying(50) COLLATE pg_catalog."default",
    model character varying(50) COLLATE pg_catalog."default",
    CONSTRAINT pk_product PRIMARY KEY (identifier, "order"),
    CONSTRAINT product_order_fkey FOREIGN KEY ("order")
        REFERENCES public."order" (identifier) MATCH SIMPLE
        ON UPDATE RESTRICT
        ON DELETE RESTRICT
);

CREATE INDEX order_order
    ON public."order" USING btree
    ("order" COLLATE pg_catalog."default")
    TABLESPACE pg_default;

CREATE INDEX order_serial
    ON public."order" USING btree
    (serial COLLATE pg_catalog."default")
    TABLESPACE pg_default;