#include <stdio.h>
#include <decoder.h>
#include <stdlib.h>
#include <BitArray/bit_array.h>

bool disassemble(Decoder *decoder, char *filein, char *fileout)
{
    int maxImageSize = decoder->getMaxImageSize();
    bool imageSizeFixed = decoder->isImageSizeFixed();

    int byteSize = maxImageSize / 8;

    FILE* fdin;
    FILE* fdout;
    fdin = fopen(filein, "rb");
    fdout = fopen(fileout, "w");

    char* d = (char*) malloc(byteSize + 1);
    int d = 0;
    while (fread(d, sizeof(char), byteSize, fdin)) {
        BIT_ARRAY* data = _bitVector_construct(d); // tut ot charov sdelay constructor
        DecoderResult result = decode(decoder, data); // tut s ukazatelyami pridumay

        if (result == null)
        {
            fclose(fdin);
            fclose(fdout);
            free(d);
            //zhopa
            return false;
        }

        IsaPrimitive *primitive = result.getPrimitive();
        char* text = primitive->text(tempVars); // Dobav tut tempvars
        fputs(text, fdout);
        putc('\n', fdout);

        if (!imageSizeFixed) {
            int bitsRead = result->getBitSize();
            // InvariantChecks

            int bytesRead = bitsRead / 8;
            fseek(fdin, -(byteSize - bytesRead), SEEK_CUR);
        }
    }

    fclose(fdin);
    fclose(fdout);
    free(d);
    return true;
}