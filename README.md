# NTM Reactors Addon (Forge 1.12.2)

Addon para **HBM's Nuclear Tech - Extended 2.0.2** (modid `hbm`). Añade 3 reactores:

| Reactor | Combustible (ranura 1) | Ranura 2 | Potencia | Mecánica |
|---|---|---|---|---|
| Neutrones Rápidos | Barra de uranio (fuel) o MOX (+50 %) | Litio (cubo o polvo) = refrigerante | 800 FE/t | Sin litio se calienta y hace SCRAM a los ~25 s |
| Torio (iniciación dual) | Barra de torio (Th-232 al 60 %) | Barra de uranio o MOX = activador | 300 FE/t | Gasta 1 activador para arrancar; luego sigue solo |
| Agua Pesada (HWR) | Uranio natural: billet, lingote o barra | Celda de agua pesada = moderador | 120 FE/t | Sin moderador no funciona |

Ranura 3 = combustible agotado (si está llena, el reactor espera a que la vacíes).
La energía sale como **Forge Energy (FE/RF)** por todas las caras hacia cualquier bloque que la acepte.
NTM usa su propia energía (HE): pon un **convertidor RF→HE de NTM** pegado al reactor para meterla en tu red NTM.

Receta de la celda de agua pesada (provisional): 1 tanque de deuterio de NTM + 1 cubo de agua = 2 celdas.

## Cómo compilarlo
1. Instala el **JDK 8**.
2. Descarga el **Forge MDK 1.12.2 (14.23.5.2860)** y descomprímelo en una carpeta.
3. Borra su carpeta `src` y copia aquí la carpeta `src` de este addon.
4. En el `build.gradle` del MDK cambia solo: `version = "0.1.0"`, `group = "com.ntmreactors"`, `archivesBaseName = "ntm-reactors-addon"`.
5. Abre la carpeta en IntelliJ IDEA como proyecto Gradle y ejecuta `setupDecompWorkspace` y luego `genIntellijRuns`.
6. Pon el jar de NTM Extended en la carpeta `run/mods/` (créala si no existe) y ejecuta `runClient`.
7. Para generar el jar final: `gradlew build` (sale en `build/libs`). Ese jar va en `mods/` junto con NTM.

El addon NO se compila contra NTM: usa sus ítems por nombre de registro (`hbm:rod_th232`, etc.), así que solo necesita Forge.

## Dónde ajustar el balance
Todo en `ReactorType.java` (potencia, buffer, combustibles, duración) y las constantes de calor en `TileEntityReactorBase.java`.

## Opción sin instalar nada: compilar en GitHub
1. Crea una cuenta en github.com y un repositorio nuevo (privado está bien).
2. Sube TODO el contenido de esta carpeta (incluida `.github`, `build.gradle` y `src`).
3. Ve a la pestaña **Actions**, abre el flujo **build** y espera a que termine (unos minutos).
4. Dentro de esa ejecución, en **Artifacts**, descarga `ntm-reactors-addon`: ahí está el `.jar`.
5. Copia el `.jar` a la carpeta `mods` de tu Minecraft 1.12.2 con Forge, junto al jar de NTM Extended.
