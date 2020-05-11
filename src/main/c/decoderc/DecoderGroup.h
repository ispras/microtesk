#include <string.h>

typedef struct DecoderGroup DecoderGroup;

struct DecoderGroup {
  int maxImageSize;
  int imageSizeFixed;
  BIT_ARRAY* opc;
  BIT_ARRAY* opcMask;
  List* decoderList;
};

DecoderGroup DecoderGroup_constructor(int maxImageSize,
                            int imageSizeFixed,
                            char* opcMask)
{
  DecoderGroup decGr = {
    .maxImageSize = maxImageSize,
    .imageSizeFixed = imageSizeFixed,
  };

  if (NULL != opc) {
    decGr.opc = bit_array_create(strlen(opc));
  } else {
    decGr.opc = NULL;
  }

  if (NULL != opcMask) {
      decGr.opcMask = bit_array_create(strlen(opcMask));
    } else {
      decGr.opcMask = NULL;
    }
}

int isOpcMatch(Decoder* dec, BIT_ARRAY* image) {
  if (NULL == dec->opc || dec->opc->num_of_bits == 0) {
    return 1;
  }

  if (dec->opcMask->num_of_bits > image->num_of_bits) {
    return 0;
  }

  BIT_ARRAY* imageOpc = applyOpcMask(dec, image);
  int compareResult = bit_array_get_wordn(imageOpc, 0, imageOpc->num_of_bits) == bit_array_get_wordn(dec->opc, 0, dec->opc->num_of_bits);
  bit_array_free(imageOrc);

  return compareResult;
}

BIT_ARRAY* applyOrcMask(Decoder* dec, BIT_ARRAY* image) {
  BIT_ARRAY* croppedImage;
  if (image->num_of_bits > dec->opcMask->num_of_bits) { // Всегда image >= opcMask?
    croppedImage = image.field(0, opcMask.getBitSize() - 1); // what is this?
  } else {
    croppedImage = image;
  }
  BIT_ARRAY* andResult = bit_array_create(dec->opcMask->num_of_bits);
  bit_array_and(andResult, croppedImage, dec->opcMask);
  return andResult;
}