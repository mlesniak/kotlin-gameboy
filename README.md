# Overview

Goal: Show the Nintendo title animation

https://www.youtube.com/watch?v=g9x6alnYvIU&ab_channel=i12bretro

## Current status

✅Started basic interpreter infrastructure

Implementing opcode by opcode, whenever an unknown is seen, throw an exception.

    PC 0x0005
    SP 0xFFFE
    A  0x00
    F  0b0000
    000000  31 fe ff af 21 ff 9f 32  cb 7c 20 fb 21 26 ff 0e  |1...!..2.| .!&..|
    000010  11 3e 80 32 e2 0c 3e f3  e2 32 3e 77 77 3e fc e0  |.>.2..>..2>ww>..|
    Exception in thread "main" java.lang.IllegalStateException: Unknown opcode 0x21 at 0x5

✅Parsed the internal logo format, i.e. when running this is shown

    00000134  54 45 54 52 49 53 00 00  00 00 00 00 00 00 00 00  |TETRIS..........|
    ██   ██ ██                             ██       
    ███  ██ ██        ██                   ██       
    ███  ██          ████                  ██       
    ██ █ ██ ██ ██ ██  ██  ████  ██ ██   █████  ████
    ██ █ ██ ██ ███ ██ ██ ██  ██ ███ ██ ██  ██ ██  ██
    ██  ███ ██ ██  ██ ██ ██████ ██  ██ ██  ██ ██  ██
    ██  ███ ██ ██  ██ ██ ██     ██  ██ ██  ██ ██  ██
    ██   ██ ██ ██  ██ ██  █████ ██  ██  █████  ████

## Next Steps

- understand rom format
- load rom format