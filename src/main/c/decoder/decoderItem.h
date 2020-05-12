#include <stdio.h>
#include <decoder.h>
#include <decoderResult.h>
#include <stdlib.h>
#include <BitArray/bit_array.h>

typedef struct DecoderItem DecoderItem;

struct DecoderItem {
  int maxImageSize;
  int imageSizeFixed;
  char* opcMask;
  char* opc;
  int position;
};

DecoderItem DecoderItem_constructor(int maxImageSize,
                            int imageSizeFixed,
                            char* opcMask,
                            char* opc)
{
  DecoderItem decItem = {
    .maxImageSize = maxImageSize,
    .imageSizeFixed = imageSizeFixed,
    .position = 0,
  };

  if (NULL != opcMask) {
      decItem.opcMask = bit_array_create(strlen(opcMask));
    } else {
      decItem.opcMask = NULL;
  }

    if (NULL != opcMask) {
      decItem.opcMask = bit_array_create(strlen(opcMask));
    } else {
      decItem.opcMask = NULL;
  }
}

BIT_ARRAY* field_image(BIT_ARRAY* v, int start, int end) {
  int offset = v->num_of_bits - 1;
  return bit_array_field(v, offset - start, offset - end);
}


int  matchNextOpc(DecoderItem* decItem, BIT_ARRAY* image, BIT_ARRAY* value) {
  int newPosition = decItem->position + value->num_of_bits;
  BIT_ARRAY* field = field_image(image, decItem->position, newPosition - 1);
  
  if (!equal_op(image, value)) {
    return false;
  }

  decItem->position = newPosition;
  return 1;
}


IsaPrimitive* readNextImmediate(DecoderItem* decItem, BIT_ARRAY* image, Type type) {
  int newPosition = decItem->position + type.bitSize;
  BIT_ARRAY* field = field_image(image, decItem->position, newPosition - 1);
  
  decItem->position = newPosition;
  return Immediate(Data_constructor(field));
}


IsaPrimitive* readNextPrimitive(DecoderItem* decItem, BIT_ARRAY* image, Decoder* decoder) {
  BIT_ARRAY* field = field_image(image, decItem->position, decoder->maxImageSize - 1);
  
  DecoderResult result = decode(decoder, field);

  if (NULL == result) {
    return NULL;
  }

  decItem->position += result.bitSize;
  return result.primitive;
}

DecoderResult newResult(DecoderItem* decItem, IsaPrimitive* isa) {
  return DecoderResult_Constructor(isa, decItem->position);
}
