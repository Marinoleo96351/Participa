create table apoio (
    duvida_id bigint not null references duvida(id),
    usuario_id bigint not null references usuario(id),
    criado_em timestamptz not null default now(),
    primary key (duvida_id, usuario_id)
);
