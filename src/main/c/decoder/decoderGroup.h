#include <stdlib.h>
#include <string.h>
#include "dynamic_array.h"
#include "BitArray/bit_array.h"

typedef struct DecoderGroup DecoderGroup;

struct DecoderGroup {
  int maxImageSize;
  int imageSizeFixed;
  char* opcMask;
  char* opc;
  Array* decoderList;
};

DecoderGroup DecoderGroup_constructor(int maxImageSize,
                            int imageSizeFixed,
                            char* opcMask)
{
  DecoderGroup decGr = {
    .maxImageSize = maxImageSize,
    .imageSizeFixed = imageSizeFixed,
  };

  if (NULL != opcMask) {
      decGr.opcMask = bit_array_create(strlen(opcMask));
    } else {
      decGr.opcMask = NULL;
  }

  decGr.decoderList = Array_constructor();
}

void add(DecoderGroup* dGroup, Decoder* decoder) {
  push_back(dGroup->decoderList, decoder);
}

DecoderResult decode_using_decoder_list(DecoderGroup* dGroup, BIT_ARRAY* image) {
  Array* decoderList = dGroup->decoderList;
  for (size_t i = 0; i < dGroup->decoderList->size; i++) {
    DecoderResult result = decode(decoderList[i], image);
    if (NULL != result) {
      return result;
    }
  }

  return NULL;
}