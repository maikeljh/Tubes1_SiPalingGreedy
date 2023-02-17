# Tubes1_SiPalingGreedy

## Daftar Isi
- [Deskripsi Singkat Galaxio](#deskripsi-singkat-galaxio)
- [Sistematika File](#sistematika-file)
- [Requirement Program](#requirement-program)
- [Spesifikasi dan Panduan](#spesifikasi-dan-panduan)
- [Build and Compile Program](#build-and-compile-program)
- [Anggota Kelompok](#anggota-kelompok)
- [Contoh Tampilan Galaxio](#contoh-tampilan-galaxio)

## Deskripsi Singkat Galaxio
Galaxio merupakan suatu permainan pertandingan antar bot kapal pemainnya yang berlatar belakang di luar angkasa. 
Setiap pemain diwakilkan oleh sebuah bot kapal pada permainan. Satu pemain yang tetap bertahan hingga akhir permainan akan menjadi pemenangnya. 
Permainan berakhir ketika hanya tersisa satu pemain yang bertahan. 
Untuk memenangkan permainan, setiap bot kapal dapat memakan makanan atau bot kapal pemain lain yang lebih kecil, menembakkan torpedo, mengaktifkan shield, dan lain-lain. 
Kemenangan dapat diraih dengan strategi permainan yang baik. Oleh karena itu, dibuatlah suatu game engine yang mengimplementasikan strategi algoritma Greedy.

## Sistematika File
```
Tubes1_SiPalingGreedy
├─── doc
|   └─── SiPalingGreedy.pdf
├─── src
│   ├─── main
│   │   └─── java
│   |       ├─── Enums
│   |       |   ├─── ObjectTypes.java
│   |       |   └─── PlayerActions.java
│   |       ├─── Models
│   |       |   ├─── GameObject.java
│   |       |   ├─── GameState.java
│   |       |   ├─── GameStateDto.java
│   |       |   ├─── PlayerAction.java
│   |       |   ├─── Position.java
│   |       |   └─── World.java
│   |       ├─── Services
│   |       |   └─── BotService.java
│   |       └─── Main.java
├─── target
│   ├─── classes
│   |    ├─── Enums
│   |    |   ├─── ObjectTypes.class
│   |    |   └─── PlayerActions.class
│   |    ├─── Models
│   |    |   ├─── GameObject.class
│   |    |   ├─── GameState.class
│   |    |   ├─── GameStateDto.class
│   |    |   ├─── PlayerAction.class
│   |    |   ├─── Position.class
│   |    |   └─── World.class
│   |    ├─── Services
│   |    |   └─── BotService.class
│   |    └─── Main.class
│   ├─── libs
│   |    └─── ...
│   ├─── maven-archiver
│   |    └─── ...
│   ├─── maven-status
│   |    └─── ...
|   └─── SiPalingGreedy.jar
├─── Dockerfile
├─── pom.xml
└─── README.md
```

## Requirement Program
- Download latest release [starter-pack.zip](https://github.com/EntelectChallenge/2021-Galaxio/releases/tag/2021.3.2)
- [Java 11](https://www.oracle.com/java/technologies/downloads/#java)
- [Intellij IDEA](https://www.jetbrains.com/idea/)
- [NodeJS](https://nodejs.org/en/download/)
- [.Net Core 3.1](https://dotnet.microsoft.com/en-us/download/dotnet/3.1)
- [Maven](https://maven.apache.org/download.cgi?.)

## Spesifikasi dan Panduan
- [Spesifikasi](https://docs.google.com/document/d/1LVNQQMdTfMw02mCO6RdTBga5wZAUfEtD/edit)
- [Panduan](https://docs.google.com/document/d/1Ym2KomFPLIG_KAbm3A0bnhw4_XQAsOKzpTa70IgnLNU/edit)

## Build and Compile Program
### Build Program
- Buka terminal pada folder JavaBot.
- Masukkan command ```mvn clean package```.
- Akan terbentuk file .jar pada folder target.

### Compile Program
- Buka terminal pada folder runner-publish dan masukkan command ```dotnet GameRunner.dll```.
- Buka terminal pada folder engine-publish dan masukkan command ```dotnet Engine.dll```.
- Buka terminal pada folder logger-publish dan masukkan command ```dotnet Logger.dll```.
- Buka terminal pada folder reference-bot-publish dan masukkan command ```dotnet ReferenceBot.dll``` untuk meregistrasikan ReferenceBot ke dalam permainan, atau <br>
masukkan command ```java -jar <path_file_jar>``` untuk meregistrasikan bot SiPalingGreedy ke dalam permainan <br>
(contoh: ```java -jar D:\Tubes1\starter-pack\starter-bots\JavaBot\target\SiPalingGreedy.jar```).
- Permainan akan dimulai setelah jumlah pemain yang terdaftar memenuhi kriteria. Permainan akan dijalankan pada terminal.

### Visualizer
- Ekstrak file .zip pada folder visualizer sesuai dengan sistem operasi yang digunakan.
- Jalankan Galaxio.exe.
- Pilih menu Option.
- Pada bagian Log Files Location, masukkan path folder logger-publish, lalu Save.
- Pilih menu Load.
- Pada bagian Game Log, pilih file JSON yang ingin ditampilkan, lalu Start.
- Game log akan divisualisasikan. Anda dapat menggunakan tombol Start, Pause, Rewind, dan Reset untuk menonton game log.

## Anggota Kelompok
| NIM       | Nama                      |
| --------- | --------------------------|
| 13521059  | Arleen Chrysantha Gunardi |
| 13521124  | Michael Jonathan Halim    |
| 13521127  | Marcel Ryan Antony        |

## Contoh Tampilan Galaxio
![Screenshot_2587](https://user-images.githubusercontent.com/87570374/219684704-a32b7ec0-0aa4-4d0a-a3c3-6017e1e1ff32.png)
<b>Tampilan Utama<b>

![Screenshot_2588](https://user-images.githubusercontent.com/87570374/219684777-1644c549-13b6-46f9-b236-445ad94f9691.png)
<b>Tampilan Permainan<b>


