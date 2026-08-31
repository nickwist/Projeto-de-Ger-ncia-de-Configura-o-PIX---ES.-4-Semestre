Camada de Serviços (`src/service`)
A camada de serviços é responsável por implementar a Lógica de Negócio.

Classe Implementada
`UsuarioService`
Classe responsável por todo o ciclo de vida do usuário no sistema (CRUD).

Principais Funcionalidades
- Cadastro: Cria novos usuários e gerencia a atribuição automática de IDs.
- Busca: Localiza usuários através de seus IDs únicos.
- Atualização: Permite a alteração de dados cadastrais de usuários existentes.
- Remoção: Exclui usuários da base de dados.

Observações Técnicas:
- Simulação de Banco de Dados: Utilizei uma `List<Usuario>` (ArrayList) para armazenar os dados em memória durante a execução do programa.
- Segurança com `Optional`: O método `buscarPorId` retorna um `Optional<Usuario>`. Isso foi implementado para evitar o erro de `NullPointerException`. 
- Como usar: Antes de acessar o usuário, utilize o método `.isPresent()` ou `.ifPresent()` para garantir que o usuário foi realmente encontrado.

// Exemplo de cadastro
Usuario novo = userService.cadastrar("Nome", "123...", "email@teste.com", "999...");

// Exemplo de busca segura
userService.buscarPorId(1).ifPresent(u -> System.out.println("Usuário encontrado: " + u.getNome()));
