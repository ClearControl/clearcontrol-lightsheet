
__kernel void downsample_3d_factor2_sum(write_only image3d_t dst, read_only image3d_t src) {
  const sampler_t sampler = CLK_NORMALIZED_COORDS_FALSE | CLK_ADDRESS_CLAMP_TO_EDGE | CLK_FILTER_NEAREST;

  const int dx = get_global_id(0);
  const int dy = get_global_id(1);
  const int dz = get_global_id(2);

  const int sx = dx * 2;
  const int sy = dy * 2;
  const int sz = dz * 2;
  DTYPE_IN out = READ_IMAGE(src,sampler,(int4)(sx+1,sy,  sz,  0)).x
           + READ_IMAGE(src,sampler,(int4)(sx+1,sy,  sz+1,0)).x
           + READ_IMAGE(src,sampler,(int4)(sx+1,sy+1,sz,  0)).x
           + READ_IMAGE(src,sampler,(int4)(sx+1,sy+1,sz+1,0)).x
           + READ_IMAGE(src,sampler,(int4)(sx,  sy,  sz,  0)).x
           + READ_IMAGE(src,sampler,(int4)(sx,  sy,  sz+1,0)).x
           + READ_IMAGE(src,sampler,(int4)(sx,  sy+1,sz,  0)).x
           + READ_IMAGE(src,sampler,(int4)(sx,  sy+1,sz+1,0)).x;


  WRITE_IMAGE(dst,(int4)(dx,dy,dz,0),(DTYPE_OUT)out);
}


__kernel void downsample_3d_factor2_sum_slice_by_slice(write_only image3d_t dst, read_only image3d_t src) {
  const sampler_t sampler = CLK_NORMALIZED_COORDS_FALSE | CLK_ADDRESS_CLAMP_TO_EDGE | CLK_FILTER_NEAREST;

  const int dx = get_global_id(0);
  const int dy = get_global_id(1);
  const int dz = get_global_id(2);

  const int sx = dx * 2;
  const int sy = dy * 2;
  const int sz = dz;
  DTYPE_IN out = READ_IMAGE(src,sampler,(int4)(sx+1,sy,  sz,  0)).x
           + READ_IMAGE(src,sampler,(int4)(sx+1,sy+1,sz,  0)).x
           + READ_IMAGE(src,sampler,(int4)(sx,  sy,  sz,  0)).x
           + READ_IMAGE(src,sampler,(int4)(sx,  sy+1,sz,  0)).x;


  WRITE_IMAGE(dst,(int4)(dx,dy,dz,0),(DTYPE_OUT)out);
}


