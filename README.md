# 🎬 AnimaIA v0.2 — Estúdio de animação em pixel art

Aplicativo **Android nativo em Java** para contar histórias curtas usando personagens personalizados e animações 2D.

## Nesta versão

- ✅ Roteiro **offline e sem API** baseado no texto do usuário (regras e palavras-chave; não é IA generativa).
- ✅ **Editor de cenas**: alterar legendas, cenário, ação; adicionar até 6 cenas e excluir até o mínimo de 2.
- ✅ Variantes de histórias baseadas na ideia, em vez do mesmo roteiro fixo.
- ✅ Pesquisa de imagens **Wikimedia Commons** integrada, sem chaves. Resultados incluem nome e licença e botão para abrir a fonte.
- ✅ Botão **Google Imagens** abre a busca normal no navegador; depois é possível importar uma imagem salva na galeria.
- ✅ 3 personagens personalizáveis, importação de PNG/WebP/JPG da galeria, modo pixel art.
- ✅ Proporções 16:9 (YouTube) e 9:16 (Shorts/Reels).
- ✅ Prévia animada, vídeos MP4 H.264, 18 fps, duração de 8, 12, 18 ou 24 segundos.
- ✅ Atualização sobre a versão 0.1 (mesmo applicationId; versionCode aumentado).

## Obter o APK pelo GitHub

Abra a aba **Actions**, escolha uma execução bem-sucedida de **Build AnimaIA APK**, baixe o arquivo **AnimaIA-v0.2-debug** e extraia o **app-debug.apk**.

O arquivo compilado fica em **Filmes/AnimaIA** depois da exportação.

## Sobre APIs

- Google Custom Search **JSON API não aceita novos clientes desde 2026** e será encerrada em 2027. Por isso o AnimaIA v0.2 não exige API key nem Engine ID para achar imagens.
- Google AI Studio / Gemini API, nos termos atuais, exige usuário de **18 anos ou mais** e proíbe disponibilizar o cliente para menores de 18. Por isso a v0.2 opta por editor criativo local e **não solicita chave Gemini**.
- O Wikimedia Commons pode ter imagens com atribuição obrigatória ou restrições de licença; sempre abra a fonte para conferir os direitos. Nem todo resultado será sprite.
- Não faça scraping do Google para contornar o encerramento da API.

## Limitações honestas da v0.2

O AnimaIA anima **sprites estáticos usando um conjunto de movimentos predefinidos**. Não é capaz de gerar animações profissionais inéditas nem sprites originais a partir de texto por um modelo generativo, ainda não suporta spritesheets animados e exporta **sem trilha sonora**. A exportação pode ser incompatível com aparelhos sem um encoder H.264 YUV420.

## Build

Java 17, Gradle 8.9, Android SDK 35, Android Gradle Plugin 8.7.3; comando:

`gradle :app:assembleDebug`

APK: `app/build/outputs/apk/debug/app-debug.apk`.

## Próximos marcos

- Sprite sheets com sprites de múltiplas poses e keyframes.
- Projetos salvos e timeline longa.
- Narração, áudio e SFX licenciados em MP4.
- Mais resoluções, animações e transições.

Crie suas próprias animações e respeite licenças de personagens, imagens e trilhas.
