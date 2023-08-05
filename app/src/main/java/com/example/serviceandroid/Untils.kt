package com.example.serviceandroid

import com.example.serviceandroid.model.Song

object Untils {

    val list: List<Song> = listOf(
        Song("Người ta có thương ...", "Trúc Nhân", R.mipmap.truc_nhan, R.raw.nguoi_ta_co_thuong_minh_dau),
        Song("Lớn rồi còn khóc nhè", "Trúc Nhân", R.mipmap.truc_nhan1, R.raw.lon_roi_con_khoc_nhe),
        Song("Waiting for u", "Mono", R.mipmap.mono, R.raw.waiting_for_u),
        Song("Muộn rồi mà sao còn", "Sơn Tùng MTP", R.mipmap.sontung, R.raw.muon_roi_ma_sao_con),
        Song("Đã Từng Là", "Vũ.", R.mipmap.vu1, R.raw.da_tung_la),
        Song("Anh Nhớ Ra", "Vũ.", R.mipmap.vu2, R.raw.anh_nho_ra),
        Song("Bước Qua Nhau", "Vũ.", R.mipmap.vu3, R.raw.buoc_qua_nhau),
        Song("Bước Qua Mùa Cô Đơn", "Vũ.", R.mipmap.vu4, R.raw.buoc_qua_mua_co_don),
        Song("Vì Anh Đâu Có Biết", "Madihu, Vũ.", R.mipmap.madihu1, R.raw.vi_anh_dau_co_biet),
        Song("Có em", "Madihu, Low G", R.mipmap.madihu2, R.raw.co_em),
        Song("3107 - P1", "W/n ft. Nâu, Duongg", R.mipmap.wn, R.raw.bamotkhongbay),
        Song("Lâu Không Cười", "Phạm Nguyên Ngọc, Bùi Trưởng Linh", R.mipmap.pnn, R.raw.lau_khong_cuoi),
        Song("Quên Đặt Tên", "Phạm Nguyên Ngọc", R.mipmap.pnn1, R.raw.quen_dat_ten),
        Song("Người Lạ Người Thương, ...", "Phạm Nguyên Ngọc", R.mipmap.pnn2, R.raw.nguoi_la_nguoi_thuong),
        Song("Your Smile", "Obito, Hmngann", R.mipmap.obito, R.raw.your_smile),
        Song("À Lôi", "Double2T, Masew", R.mipmap.aloi, R.raw.aloi),
        Song("Không Còn Em", "Madihu", R.mipmap.madihu, R.raw.khong_con_em_madihu),
        Song("Vành Khuyên Nhỏ", "Liu Grace", R.drawable.music, R.raw.vanhkhuyennho),
        Song("Về Quê", "Mikelodic", R.mipmap.mikelodic, R.raw.veque),)
    var musicCount = Untils.list.size
    var musicCurrent = -1;
    var musicChange = -1;
    var isReapeat = false
    var isRandom = false
}