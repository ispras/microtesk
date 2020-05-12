typedef struct DecoderResult DecoderResult;

struct DecoderResult {
  IsaPrimitive* primitive;
  int bitSize;
};

DecoderResult DecoderResult_Constructor(IsaPrimitive* primitive, int bitSize) {
  DecoderResult res = {
          .primitive = primitive,
          .bitSize = bitSize,
  };

  return res;
}