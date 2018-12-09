package com.pengcit.jorfelag.pengolahancitra.face_detection;

import android.graphics.Point;

public class FaceDataset {

    public static String[] LABELS = new String[]{
            "Roland",
            "Felix",
            "Agus",
            "Jordhy"
    };

    // ORDER: left eyebrow, right eyebrow, left eye, right eye, left nose, right nose, mouth
    public static Point[][][] CONTROL_POINTS = new Point[][][]{
            {
                    {
                            new Point(216, 221),
                            new Point(226, 217),
                            new Point(236, 207),
                            new Point(246, 208),
                            new Point(256, 208),
                            new Point(268, 217),
                            new Point(256, 224),
                            new Point(246, 224),
                            new Point(236, 224),
                            new Point(226, 222)
                    },
                    {
                            new Point(327, 210),
                            new Point(338, 207),
                            new Point(349, 206),
                            new Point(360, 206),
                            new Point(371, 210),
                            new Point(383, 220),
                            new Point(371, 224),
                            new Point(360, 219),
                            new Point(349, 222),
                            new Point(338, 222)
                    },
                    {
                            new Point(207, 251),
                            new Point(218, 244),
                            new Point(229, 241),
                            new Point(240, 241),
                            new Point(251, 243),
                            new Point(265, 257),
                            new Point(251, 261),
                            new Point(240, 263),
                            new Point(229, 264),
                            new Point(218, 252)
                    },
                    {
                            new Point(332, 252),
                            new Point(344, 241),
                            new Point(356, 240),
                            new Point(368, 240),
                            new Point(380, 244),
                            new Point(393, 254),
                            new Point(380, 253),
                            new Point(368, 262),
                            new Point(356, 263),
                            new Point(344, 261)
                    },
                    {
                            new Point(269, 336),
                            new Point(275, 328),
                            new Point(281, 328),
                            new Point(287, 329),
                            new Point(293, 332),
                            new Point(298, 340),
                            new Point(293, 341),
                            new Point(287, 340),
                            new Point(281, 337),
                            new Point(275, 338)
                    },
                    {
                            new Point(311, 330),
                            new Point(314, 329),
                            new Point(317, 328),
                            new Point(320, 328),
                            new Point(323, 328),
                            new Point(329, 331),
                            new Point(323, 338),
                            new Point(320, 338),
                            new Point(317, 337),
                            new Point(314, 337)
                    },
                    {
                            new Point(275, 386),
                            new Point(285, 381),
                            new Point(295, 385),
                            new Point(305, 379),
                            new Point(315, 380),
                            new Point(326, 383),
                            new Point(315, 397),
                            new Point(305, 392),
                            new Point(295, 388),
                            new Point(285, 388)
                    }
            },
            {
                    {
                            new Point(128, 219),
                            new Point(139, 213),
                            new Point(150, 214),
                            new Point(161, 216),
                            new Point(172, 219),
                            new Point(186, 230),
                            new Point(172, 238),
                            new Point(161, 244),
                            new Point(150, 244),
                            new Point(139, 229)
                    },
                    {
                            new Point(226, 229),
                            new Point(241, 220),
                            new Point(256, 215),
                            new Point(271, 212),
                            new Point(286, 216),
                            new Point(301, 226),
                            new Point(286, 228),
                            new Point(271, 229),
                            new Point(256, 229),
                            new Point(241, 224)
                    },
                    {
                            new Point(128, 251),
                            new Point(140, 244),
                            new Point(152, 243),
                            new Point(164, 244),
                            new Point(176, 250),
                            new Point(191, 262),
                            new Point(176, 263),
                            new Point(164, 262),
                            new Point(152, 264),
                            new Point(140, 261)
                    },
                    {
                            new Point(230, 254),
                            new Point(243, 249),
                            new Point(256, 243),
                            new Point(269, 241),
                            new Point(282, 244),
                            new Point(296, 255),
                            new Point(282, 259),
                            new Point(269, 266),
                            new Point(256, 259),
                            new Point(243, 264)
                    },
                    {
                            new Point(169, 321),
                            new Point(183, 326),
                            new Point(197, 318),
                            new Point(211, 306),
                            new Point(225, 319),
                            new Point(240, 323),
                            new Point(225, 331),
                            new Point(211, 332),
                            new Point(197, 333),
                            new Point(183, 335)
                    },
                    {
                            new Point(169, 321),
                            new Point(183, 326),
                            new Point(197, 318),
                            new Point(211, 306),
                            new Point(225, 319),
                            new Point(240, 323),
                            new Point(225, 331),
                            new Point(211, 332),
                            new Point(197, 333),
                            new Point(183, 335)
                    },
                    {
                            new Point(167, 364),
                            new Point(184, 356),
                            new Point(201, 348),
                            new Point(218, 348),
                            new Point(235, 355),
                            new Point(254, 364),
                            new Point(235, 367),
                            new Point(218, 385),
                            new Point(201, 384),
                            new Point(184, 367)
                    }
            },
            {
                    null,
                    null,
                    {
                            new Point(357, 555),
                            new Point(372, 547),
                            new Point(387, 541),
                            new Point(402, 541),
                            new Point(417, 543),
                            new Point(432, 556),
                            new Point(417, 560),
                            new Point(402, 559),
                            new Point(387, 560),
                            new Point(372, 558)
                    },
                    {
                            new Point(509, 552),
                            new Point(525, 536),
                            new Point(541, 531),
                            new Point(557, 532),
                            new Point(573, 536),
                            new Point(588, 542),
                            new Point(573, 547),
                            new Point(557, 552),
                            new Point(541, 553),
                            new Point(525, 552)
                    },
                    {
                            new Point(424, 651),
                            new Point(427, 635),
                            new Point(430, 631),
                            new Point(433, 656),
                            new Point(436, 657),
                            new Point(441, 658),
                            new Point(436, 662),
                            new Point(433, 657),
                            new Point(430, 655),
                            new Point(427, 654)
                    },
                    {
                            new Point(491, 651),
                            new Point(499, 647),
                            new Point(507, 647),
                            new Point(515, 651),
                            new Point(523, 632),
                            new Point(530, 640),
                            new Point(523, 654),
                            new Point(515, 655),
                            new Point(507, 654),
                            new Point(499, 653)
                    },
                    {
                            new Point(419, 707),
                            new Point(444, 706),
                            new Point(469, 708),
                            new Point(494, 694),
                            new Point(519, 698),
                            new Point(544, 698),
                            new Point(519, 708),
                            new Point(494, 714),
                            new Point(469, 713),
                            new Point(444, 710)
                    }
            },
            {
                    {
                            new Point(144, 217),
                            new Point(159, 197),
                            new Point(174, 195),
                            new Point(189, 199),
                            new Point(204, 205),
                            new Point(219, 212),
                            new Point(204, 213),
                            new Point(189, 214),
                            new Point(174, 211),
                            new Point(159, 213)
                    },
                    {
                            new Point(274, 211),
                            new Point(288, 203),
                            new Point(302, 195),
                            new Point(316, 193),
                            new Point(330, 195),
                            new Point(345, 210),
                            new Point(330, 213),
                            new Point(316, 210),
                            new Point(302, 212),
                            new Point(288, 214)
                    },
                    {
                            new Point(155, 247),
                            new Point(167, 237),
                            new Point(179, 230),
                            new Point(191, 232),
                            new Point(203, 232),
                            new Point(214, 244),
                            new Point(203, 255),
                            new Point(191, 253),
                            new Point(179, 254),
                            new Point(167, 251)
                    },
                    {
                            new Point(278, 244),
                            new Point(290, 232),
                            new Point(302, 229),
                            new Point(314, 231),
                            new Point(326, 234),
                            new Point(338, 243),
                            new Point(326, 255),
                            new Point(314, 254),
                            new Point(302, 258),
                            new Point(290, 250)
                    },
                    {
                            new Point(200, 301),
                            new Point(201, 300),
                            new Point(202, 294),
                            new Point(203, 292),
                            new Point(204, 292),
                            new Point(207, 287),
                            new Point(204, 296),
                            new Point(203, 299),
                            new Point(202, 300),
                            new Point(201, 305)
                    },
                    {
                            new Point(210, 296),
                            new Point(226, 300),
                            new Point(242, 300),
                            new Point(258, 291),
                            new Point(274, 300),
                            new Point(290, 304),
                            new Point(274, 311),
                            new Point(258, 311),
                            new Point(242, 311),
                            new Point(226, 313)
                    },
                    {
                            new Point(200, 352),
                            new Point(220, 335),
                            new Point(240, 330),
                            new Point(260, 329),
                            new Point(280, 333),
                            new Point(299, 350),
                            new Point(280, 349),
                            new Point(260, 346),
                            new Point(240, 347),
                            new Point(220, 350)
                    }
            },
    };
}
