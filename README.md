# Overview

Initial goal: Show the Nintendo title animation when a gameboy boots up [reference video](https://www.youtube.com/watch?v=g9x6alnYvIU&ab_channel=i12bretro).



## Current status

**ABANDONED** -- I've implement all CPU instructions to simulate the necessary CPU instructions from 
0x00 until 0xFF, i.e. the whole boot rom. The interpreter fails after all of the boot rom has successfully
been interpreted, i.e. at position 0x100.
```
Unknown opcode 0x00 at position 0x0100
PC=0100 SP=FFFE
A=01 B=00 C=13 D=00 E=D8 H=01 L=4D
Z1 N0 H0 C0
000000F0  f5 06 19 78 86 23 05 20  fb 86 20 fe 3e 01 e0 50  |...x.#. .. .>..P|
00000100  00 c3 50 01 ce ed 66 66  cc 0d 00 0b 03 73 00 83  |..P...ff.....s..|
00000110  00 0c 00 0d 00 08 11 1f  88 89 00 0e dc cc 6e e6  |..............n.|
```
Unfortunately, I would now have to implement the whole VRAM logic and interrupt
handling (see https://gbdev.gg8.se/wiki/articles/Video_Display#VRAM_Tile_Data), 
which is seemingly quite cumbersome and not very interesting for now. 

Instead of forcing myself to spend my free time implementing something which
I find not interesting, I'm spending more time with the other pet projects
which I'm more interested in for the time being ;-) #YOLO

## Fun fact
In `com.mlesniak.gameboy.CPU.showLogo`, I've implement a method to interpret the boot rom logo data and show the
parsed logo. 

```
00000000  31 fe ff af 21 ff 9f 32  cb 7c 20 fb 21 26 ff 0e  |1...!..2.| .!&..|
00000010  11 3e 80 32 e2 0c 3e f3  e2 32 3e 77 77 3e fc e0  |.>.2..>..2>ww>..|
00000020  47 11 04 01 21 10 80 1a  cd 95 00 cd 96 00 13 7b  |G...!..........{|
00000030  fe 34 20 f3 11 d8 00 06  08 1a 13 22 23 05 20 f9  |.4 ........"#. .|
00000040  3e 19 ea 10 99 21 2f 99  0e 0c 3d 28 08 32 0d 20  |>....!/...=(.2. |
00000050  f9 2e 0f 18 f3 67 3e 64  57 e0 42 3e 91 e0 40 04  |.....g>dW.B>..@.|
00000060  1e 02 0e 0c f0 44 fe 90  20 fa 0d 20 f7 1d 20 f2  |.....D.. .. .. .|
00000070  0e 13 24 7c 1e 83 fe 62  28 06 1e c1 fe 64 20 06  |..$|...b(....d .|
00000080  7b e2 0c 3e 87 e2 f0 42  90 e0 42 15 20 d2 05 20  |{..>...B..B. .. |
00000090  4f 16 20 18 cb 4f 06 04  c5 cb 11 17 c1 cb 11 17  |O. ..O..........|
000000A0  05 20 f5 22 23 22 23 c9  ce ed 66 66 cc 0d 00 0b  |. ."#"#...ff....|
000000B0  03 73 00 83 00 0c 00 0d  00 08 11 1f 88 89 00 0e  |.s..............|
000000C0  dc cc 6e e6 dd dd d9 99  bb bb 67 63 6e 0e ec cc  |..n.......gcn...|
000000D0  dd dc 99 9f bb b9 33 3e  3c 42 b9 a5 b9 a5 42 3c  |......3><B....B<|
000000E0  21 04 01 11 a8 00 1a 13  be 20 fe 23 7d fe 34 20  |!........ .#}.4 |
000000F0  f5 06 19 78 86 23 05 20  fb 86 20 fe 3e 01 e0 50  |...x.#. .. .>..P|
██   ██ ██                             ██       
███  ██ ██        ██                   ██       
███  ██          ████                  ██       
██ █ ██ ██ ██ ██  ██  ████  ██ ██   █████  ████ 
██ █ ██ ██ ███ ██ ██ ██  ██ ███ ██ ██  ██ ██  ██
██  ███ ██ ██  ██ ██ ██████ ██  ██ ██  ██ ██  ██
██  ███ ██ ██  ██ ██ ██     ██  ██ ██  ██ ██  ██
██   ██ ██ ██  ██ ██  █████ ██  ██  █████  ████ 

```