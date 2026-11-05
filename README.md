# The Masked Morte: Shadow Army (addon)

Addon para Minecraft 1.21.1 / Fabric / Java 21. Requer Fabric API e o mod original `themaskedmorte`.

## O que esta primeira versão implementa
- Quando o jogador mata um `MobEntity`, o tipo do mob é salvo no mundo como uma sombra capturada.
- A coleção é persistente: continua salva depois de fechar e abrir o mundo.
- Cada jogador tem sua própria coleção, limitada a 100 capturas.
- `/shadowarmy list` mostra os tipos e quantidades capturados.
- `/shadowarmy summon` invoca todas as sombras armazenadas perto do jogador.
- As cópias recebem o nome `[Sombra]` e não são capturadas novamente.

## Limitações desta primeira versão
Esta é a base funcional do sistema de captura e invocação. As entidades invocadas são cópias do mob original e mantêm o comportamento de IA padrão do Minecraft. Ainda não há ordens de seguir/proteger/ficar, interface gráfica, progressão de nível, nem habilidades especiais transferidas ao jogador. Chefes e entidades especiais podem ter particularidades de spawn e devem ser testados individualmente.

## Compilação no GitHub
1. Envie todos os arquivos para um repositório.
2. Adicione o Gradle Wrapper (`gradlew`, `gradlew.bat` e `gradle/wrapper/gradle-wrapper.properties`).
3. Abra **Actions > Build**.
4. Baixe o artefato `TheMaskedMorte-Shadows`.
5. Coloque o JAR junto com Fabric API e o mod original na pasta `mods`.

> Este repositório é um addon separado; ele não substitui o JAR original nem contém o código-fonte original do sistema `/morph`.
