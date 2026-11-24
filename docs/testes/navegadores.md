# Roteiro de testes em navegadores (US15)

Critério de aceitação da US15: a aplicação funciona nas versões atuais de Chrome, Firefox,
Edge e Safari, o leiaute se adapta a partir de 360 px de largura, e o acesso à aula é feito
por código ou endereço, sem instalar nada.

## Como rodar o roteiro

Em cada navegador/dispositivo da tabela abaixo:

1. Abrir `login.html`, criar conta de estudante e entrar.
2. Entrar em uma turma pelo código (`entrar.html?codigo=XXXXXX`) e confirmar que chega na
   tela da aula com o cabeçalho, o cartão de envio de dúvida e a lista "Dúvidas da aula".
3. Reduzir a largura da janela (ou usar o modo de dispositivo do navegador) até 360 px:
   - Não deve aparecer rolagem lateral na página (só é aceitável rolagem dentro de uma
     tabela, como a de aulas do professor).
   - Os botões, checkboxes e links devem continuar fáceis de tocar (sem ficar menores que
     o dedo).
4. Enviar uma dúvida, apoiar uma dúvida de outra pessoa (ou testar com dois perfis).
5. Entrar como professor numa segunda aba/perfil, abrir a turma e confirmar que o endereço
   mostrado no cartão da turma é o endereço real do servidor (não mais `participa.app`).
6. Testar a tela de login/cadastro com o seletor de perfil, em 360 px.

## Resultado

| Navegador | Celular | Computador | Resultado | Observações |
|---|---|---|---|---|
| Chrome | | | | |
| Firefox | | | | |
| Edge | | | | |
| Safari | | | | |

Preencher "Resultado" com OK ou o defeito encontrado (e abrir uma `fix/<descricao>` se for
o caso). Esta tabela não foi preenchida pela IA — ela não tem como abrir um navegador real
nesta sessão; preencher à mão depois de rodar o roteiro acima.
