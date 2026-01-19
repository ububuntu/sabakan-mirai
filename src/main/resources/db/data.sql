-- ユーザマスタ
INSERT INTO user_m (
    user_id, user_name, user_address, password, user_role, user_valid, created_at, lasted_at
) VALUES (
    'U001', 'テスト太郎', '北海道札幌市', 'password123', 'ADMIN', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
);

-- 面接テーブル
INSERT INTO interview_t (
    interview_id, user_id, interview_eyes, interview_posture, interview_voice, interview_date, interview_score
) VALUES (
    'I001', 'U001', 4, 5, 3, CURRENT_DATE, 12
);

-- ESテーブル
INSERT INTO es_t (
    es_id, user_id, es_content_reason, es_content_selfpr, es_content_activities, es_content_stwe, es_occupation, es_date
) VALUES (
    'E001', 'U001', '志望動機の例', '自己PRの例', '学生時代の活動例', 'ストレス耐性の例', 'エンジニア', CURRENT_DATE
);

-- =============================================
-- SPI (70問)
-- =============================================
INSERT INTO spi_m (spi_id, spi_content, spi_answer1, spi_answer2, spi_answer3, spi_answer4, spi_correct_answer, spi_category
) VALUES
('S_LANG_001', '最初：最後 の関係に最も近いものを選べ。', '原因：結果', '入口：出口', '開始：終了', '左：右', 3, '言語'),
('S_LANG_002', '鉛筆：書く の関係に最も近いものを選べ。', '包丁：切る', '消しゴム：消す', '紙：折る', '椅子：座る', 1, '言語'),
('S_LANG_003', '義務：権利 の関係に最も近いものを選べ。', '需要：供給', '自由：束縛', '原因：結果', '販売：購入', 1, '言語'),
('S_LANG_004', '医者：診察 の関係に最も近いものを選べ。', '教師：授業', '歌手：歌', '料理人：包丁', '選手：試合', 1, '言語'),
('S_LANG_005', '海：水 の関係に最も近いものを選べ。', '森：木', '空：鳥', '砂漠：暑い', '山：高い', 1, '言語'),
('S_NON_041', 'PはQより重い。QはRより重い。最も軽いのは？', 'P', 'Q', 'R', '分からない', 3, '非言語'),
('S_NON_042', 'AはBの左にいる。BはCの左にいる。一番右にいるのは？', 'A', 'B', 'C', '分からない', 3, '非言語'),
('S_NON_043', 'XはYより高い。ZはXより高い。最も高いのは？', 'X', 'Y', 'Z', '分からない', 3, '非言語'),
('S_NON_044', 'あるクラスの生徒は全員メガネをかけている。太郎はこのクラスの生徒である。正しいのは？', '太郎はメガネをかけている', '太郎はメガネをかけていない', '太郎は目が悪い', '分からない', 1, '非言語'),
('S_NON_045', '全てのカラスは黒い。これはカラスである。結論として正しいのは？', 'これは黒い', 'これは白い', 'これは鳥である', '分からない', 1, '非言語');