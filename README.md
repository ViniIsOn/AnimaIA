# AnimaIA ✦ 🎬

Estúdio Android de animação pixel art 2D para criar histórias curtas com personagens e cenários originais.

## Recursos da versão 0.1

- Ideia por texto e roteiro de 4–6 cenas com **Gemini** (chave própria).
- Modo de demonstração local sem chave: roteiro por regras, **não IA gerativa**.
- Aspectos **16:9 e 9:16** para YouTube, Reels e Shorts.
- Prévia animada em loop, legendas e cenários 2D.
- Duração 8, 12, 18 ou 24 segundos.
- Importar sprites PNG/WEBP/JPG na galeria, idealmente com fundo transparente.
- Personagens em três posições: herói, rival e amigo.
- Buscador oficial Google Programmable Search com imagens integradas, quando a conta possui API key e Search Engine ID (cx).
- Fallback para abrir Google Imagens no navegador.
- Exportar MP4 H.264 sem áudio em **768×432** ou **432×768**, 18 fps, na pasta **Filmes/AnimaIA**.
- Compartilhar MP4 dentro do aplicativo.

## Instalar pelo celular

1. Abra **Actions** no repositório.
2. Escolha **Build AnimaIA APK** e abra a execução mais recente.
3. Baixe o artifact **AnimaIA-v0.1-debug** (um ZIP).
4. Extraia o ZIP e instale **app-debug.apk** no Android.

O GitHub Actions precisa estar habilitado. Instale apenas APKs de fontes em que confia.

## Chaves e limitações

Abra **Configurar IA e busca Google** dentro do app. As chaves são privadas, salvas apenas no telefone, não no repositório. Não coloque suas credenciais em commits, issues ou screenshots.

Para roteiros IA use uma chave da API Gemini (Google AI Studio). Sem chave, o roteiro é um exemplo automático por regras.

Para ver imagens do Google dentro do app, preencha credenciais da Custom Search JSON API e do Programmable Search Engine (cx), devidamente habilitadas na sua conta. O acesso oficial pode estar sujeito a disponibilidade, cota e cobranças. Sem credenciais, a busca normal abre no navegador e você pode baixar e importar PNGs manualmente. O aplicativo não faz scraping do Google.

**Importante:** a IA gera um **roteiro** e o motor anima os sprites com movimentos predefinidos. Isso ainda NÃO gera filmes completos ou sprites inéditos do zero. A versão 0.1 exporta **sem som**, sem narração e sem lipsync. Alguns aparelhos podem não possuir codec H.264 no formato necessário. Verifique direitos de uso dos sprites antes de publicar.

## Compilação

Java 17, Android SDK 35, Gradle 8.9, Android Gradle Plugin 8.7.3, minSdk 29.

Comando: **gradle :app:assembleDebug**

Arquivo final: **app/build/outputs/apk/debug/app-debug.apk**.

Projeto Android nativo Java, sem dependências externas em runtime.

## Próximas etapas

Editor de timeline, animação de sprite sheets, mais movimentos, áudio/SFX na exportação, gravação de projetos e uma API opcional de geração de imagens/vídeos.
