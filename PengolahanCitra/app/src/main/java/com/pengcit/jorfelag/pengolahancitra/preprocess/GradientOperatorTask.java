package com.pengcit.jorfelag.pengolahancitra.preprocess;

public class GradientOperatorTask extends MultipleKernelTask {

    private final static double SQRT_2 = Math.sqrt(2);

    private final static double[][][][] KERNEL = {
            {   // SOBEL
                    {   // X
                            {1, 0, -1},
                            {2, 0, -2},
                            {1, 0, -1}
                    },
                    {   // Y
                            { 1,  2,  1},
                            { 0,  0,  0},
                            {-1, -2, -1}
                    }
            },
            {   // SCHARR
                    {   // X
                            {3,  0,  -3},
                            {10, 0, -10},
                            {3,  0,  -3}
                    },
                    {   // Y
                            { 3,  10,  3},
                            { 0,   0,  0},
                            {-3, -10, -3}
                    }
            },
            {   // PREWITT
                    {   // X
                            {1, 0, -1},
                            {1, 0, -1},
                            {1, 0, -1}
                    },
                    {   // Y
                            { 1,  1,  1},
                            { 0,  0,  0},
                            {-1, -1, -1}
                    }
            },
            {   // ROBERTS (Padded)
                    {   // X
                            {1,  0, 0},
                            {0, -1, 0},
                            {0,  0, 0}
                    },
                    {   // Y
                            { 0, 1, 0},
                            {-1, 0, 0},
                            { 0, 0, 0}
                    }
            },
            {   // FREI-CHEN
                    {   // G1
                            { 1,  SQRT_2,  1},
                            { 0,       0,  0},
                            {-1, -SQRT_2, -1}
                    },
                    {   // G2
                            {     1, 0,      -1},
                            {SQRT_2, 0, -SQRT_2},
                            {     1, 0,      -1}
                    },
                    {   // G3
                            {      0, -1, SQRT_2},
                            {      1,  0,     -1},
                            {-SQRT_2,  1,      0}
                    },
                    {   // G4
                            {SQRT_2, -1,       0},
                            {    -1,  0,       1},
                            {     0,  1, -SQRT_2}
                    },
                    {   // G5
                            { 0,   1/6,  0},
                            {-1/6, 0,   -1/6},
                            { 0,   1/6,  0}
                    },
                    {   // G6
                            {-1/6, 0,  1/6},
                            {   0, 0,    0},
                            { 1/6, 0, -1/6}
                    },
                    {   // G7
                            { 1/6, -2/6,  1/6},
                            {-2/6,  4/6, -2/6},
                            { 1/6, -2/6,  1/6}
                    },
                    {   // G8
                            {-2, 1, -2},
                            { 1, 4,  1},
                            {-2, 1, -2}
                    },
                    {   // G9
                            {1/9, 1/9, 1/9},
                            {1/9, 1/9, 1/9},
                            {1/9, 1/9, 1/9}
                    }

            }
    };

    public GradientOperatorTask(PreprocessOperatorFragment fr, int kernel) {
        super(fr, KERNEL[kernel]);
    }
}
