create table termometro (
    id bigserial primary key,
    aula_id bigint not null references aula(id),
    numero integer not null,
    aberto_em timestamptz not null default now(),
    duracao_segundos integer not null check (duracao_segundos between 15 and 600),
    encerrado_em timestamptz,
    unique (aula_id, numero)
);

create table resposta_termometro (
    termometro_id bigint not null references termometro(id),
    usuario_id bigint not null references usuario(id),
    opcao varchar(20) not null,
    respondida_em timestamptz not null default now(),
    primary key (termometro_id, usuario_id)
);