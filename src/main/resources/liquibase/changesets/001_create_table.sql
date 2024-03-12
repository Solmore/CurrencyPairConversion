create table if not exists currency_schema.currency_pairs
(
    base_currency    varchar not null,
    convert_currency varchar not null,
    base_amount      numeric(30,6)  not null,
    convert_amount   numeric(30,6)  not null,
    primary key (base_currency, convert_currency, base_amount)
);