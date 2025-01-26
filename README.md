# CourseAnalyzer Projekt

Dette projekt er designet til at crawle DTU's kursusdatabase og præsentere information i en grafisk brugergrænseflade (GUI). Programmet giver mulighed for at søge, filtrere og sortere kurser baseret på forskellige kriterier.

## Sådan kører du projektet

1. **Åbn projektet i Visual Studio Code (VSC):**
   - Start VSC, og åbn projektmappen.

2. **Tilføj nødvendige biblioteker:**
   - I Visual Studio Code skal du navigere til **Java Projects** i bunden af sidebaren.
   - Klik på **Referenced Libraries**, og tilføj de biblioteker, der er inkluderet i mappen `lib`:
     - Højreklik på **Referenced Libraries**.
     - Vælg **Add JAR/Folder...** og find mappen `lib` i projektet.
     - Vælg alle `.jar`-filerne og tilføj dem.

3. **Kør programmet:**
   - Find filen `CourseAnalyzer.java` i projektets `src`-mappe.
   - Højreklik på filen og vælg **Run Java** for at starte programmet.

## Krav
- Java Development Kit (JDK) version 8 eller nyere.
- Visual Studio Code med Java-udvidelser installeret.

## Fejlfinding
- Hvis programmet ikke kører korrekt, skal du sikre dig, at alle biblioteker i `lib`-mappen er tilføjet til **Referenced Libraries** i VSC.
- Hvis du oplever problemer med GUI’en, skal du sikre, at du bruger Java 8 eller nyere.

