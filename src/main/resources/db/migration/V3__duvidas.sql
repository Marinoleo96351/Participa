create table duvida (
    id bigserial primary key,
    aula_id bigint not null references aula(id),
    autor_id bigint not null references usuario(id),
    anonima boolean not null,
    texto varchar(500) not null,
    status varchar(20) not null,
    termo_retido varchar(80),
    pos_aula boolean not null default false,
    respondida boolean not null default false,
    criada_em timestamptz not null default now()
);
