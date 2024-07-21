INSERT INTO websoso_db.user (avatar_id, birth, is_profile_public, user_id, email, intro, nickname, gender, role) VALUES (1, 2003, 1, 1, 'rina@gmail.com', '안녕하세요', '기옹', 'F', 'USER');
INSERT INTO websoso_db.user (avatar_id, birth, is_profile_public, user_id, email, intro, nickname, gender, role) VALUES (1, 2002, 1, 2, 'shkim@gmail.com', '안녕하세요', '헌', 'M', 'USER');

INSERT INTO websoso_db.genre (genre_id, genre_image, genre_name) VALUES (1, 'ro_img', '로맨스');
INSERT INTO websoso_db.genre (genre_id, genre_image, genre_name) VALUES (2, 'rf_img', '로판');
INSERT INTO websoso_db.genre (genre_id, genre_image, genre_name) VALUES (3, 'fa_img', '판타지');
INSERT INTO websoso_db.genre (genre_id, genre_image, genre_name) VALUES (4, 'mf_img', '현판');
INSERT INTO websoso_db.genre (genre_id, genre_image, genre_name) VALUES (5, 'wu_img', '무협');
INSERT INTO websoso_db.genre (genre_id, genre_image, genre_name) VALUES (6, 'dr_img', '드라마');
INSERT INTO websoso_db.genre (genre_id, genre_image, genre_name) VALUES (7, 'my_img', '미스터리');
INSERT INTO websoso_db.genre (genre_id, genre_image, genre_name) VALUES (8, 'ln_img', '라노벨');
INSERT INTO websoso_db.genre (genre_id, genre_image, genre_name) VALUES (9, 'bl_img', 'BL');

INSERT INTO websoso_db.platform (platform_id, platform_image, platform_name) VALUES (1, 'kakaopage_img', '카카오페이지');
INSERT INTO websoso_db.platform (platform_id, platform_image, platform_name) VALUES (2, 'naverseries_img', '네이버시리즈');
INSERT INTO websoso_db.platform (platform_id, platform_image, platform_name) VALUES (3, 'ridi_img', '리디');

INSERT INTO websoso_db.novel (novel_id, author, is_completed, novel_description, novel_image, title) VALUES (1, '윤이수', 0, '철저한 금녀(禁女)의 구역. 
환관들의 은밀한 세상에 한 여인이 뛰어들었다

19세기, 조선. 
여인과 관련한 일이라면 해결 못하는 일이 없는 ‘여자문제 고민상담 전문가’ 삼놈이는 사실 남장여인이다. 
어느 날 우연히 만난 화초서생으로 인해 팔자에도 없는 환관까지 되었으니……. 
철저한 금녀(禁女)의 구역. 
환관들의 은밀한 세상에 한 여인이 뛰어들었다. 
그녀의 고민 상담에 구중궁궐이 들썩이기 시작한다.', 'https://comicthumb-phinf.pstatic.net/20141010_110/pocket_1412915784499GktmY_JPEG/cloud.jpg?type=m260', '구르미 그린 달빛 [독점]');
INSERT INTO websoso_db.novel (novel_id, author, is_completed, novel_description, novel_image, title) VALUES (2, '노승아', 0, '대한민국 예비고3에게 갑자기 떨어진 날벼락. 
“뭐? 결혼을 하라구요?” 
게다가 상대는……, 
“나도 너 같은 것 관심 없어.” 
입만 열면 독설을 퍼붓던 선생님이 정혼자? 하필이면 왜? 
그리고 1년 후 벌어지는 그들의 한지붕 첩첩산중 로맨스. 
살얼음판 같은 결혼생활이 이제 막 펼쳐진다. 
풋내 작렬, 의욕 충만, 심쿵 주의, 
독사 남편과 어린 새댁의 꿀향기 가득한 결혼 정복기! 
어서 와, 결혼은 처음이지?', 'https://comicthumb-phinf.pstatic.net/20160216_216/pocket_14556178273891qpTh_JPEG/h300.jpg?type=m260', '허니허니 웨딩');
INSERT INTO websoso_db.novel (novel_id, author, is_completed, novel_description, novel_image, title) VALUES (3, '달콤J', 0, '몇 번이고 배신당해도 받아줄게.
기꺼이 이용당해줄게!

사랑하는 여자의 배신으로 아픔을 간직한 채 살아온 청운그룹 후계자 반지후. 
그리고 그의 하나뿐인 연인이었던 공은율. 
5년이 지난 후, 두 사람이 다시 만났다. 그것도 부부라는 이름으로. 
‘몇 번이고 배신당해도 받아줄게. 기꺼이 이용당해줄게. 그러니까 처음부터 다시 시작하자.’ 
두 사람은 5년이라는 시간과 서로의 아픔을 뛰어넘어 다시 사랑할 수 있을까.', 'https://comicthumb-phinf.pstatic.net/20150202_224/pocket_14228417795267p3Ua_JPEG/%BF%F8%C7%CF%B4%C2_%B0%C7_%B3%CA_%C7%CF%B3%AA_%C7%A5%C1%F6%28%C0%CE%C5%B8%C0%CC%C6%B2%29.jpg?type=m260', '원하는 건 너 하나');
INSERT INTO websoso_db.novel (novel_id, author, is_completed, novel_description, novel_image, title) VALUES (4, '윤이수', 0, '미래를 예언하는 신비한 능력을 가졌으나, 
운명을 거부하고 도망치는 여인 해루. 
어느 날 그녀 앞에 엉뚱한 선비가 나타났다. 
천하제일 길치에 공갈을 일삼는 사악한 성품. 
싸움은 몸이 아니라 머리로 하는 거라 주장하는 해괴한 사내. 
그를 만난 순간, 해루의 멈춰진 운명이 다시 돌기 시작한다.
15세기, 조선. 
실록이 기록하지 못한 조선 최고의 천재 군주와 운명의 사슬에 매인 신비한 여인. 오덕(五德)을 갖춘 조선 과학자들이 함께하는 두 사람의 달콤아슬한 사랑과 전쟁!', 'https://comicthumb-phinf.pstatic.net/20160328_97/pocket_1459135510506rEcLr_JPEG/night_mirage2.jpg?type=m260', '해시의 신루');
INSERT INTO websoso_db.novel (novel_id, author, is_completed, novel_description, novel_image, title) VALUES (5, 'Lunar 이지연', 0, '임시로 돌보던 사고뭉치 고양이를 찾아 호텔 VVIP 게스트 하우스까지 들어가게 된 인턴 세희.
그런데 하필 그곳은 하나 그룹 이재현 전무가 머무르는 곳.
그는 그룹 내 비리를 파헤치기 위해 평직원으로 위장 근무 중이다.
고양이를 잡는 순간 재현이 들어오고 부랴부랴 욕실로 숨는 세희.
잠시 후, 관능적인 숨소리와 함께 뜨거운 물줄기가 쏟아지고, 뿌연 수증기가 욕실을 가득 채운다.
촤악―. 갑자기 샤워 커튼이 열리고, 세희는 재현의 싸늘한 시선과 마주치는데……. 
“마지막 근무하는 날까지 내 눈앞에 나타나지 말도록.”', 'https://comicthumb-phinf.pstatic.net/20160127_159/pocket_1453872545950Jk8na_JPEG/crazy4u_cover.jpg?type=m260', '미치도록 너만을');
INSERT INTO websoso_db.novel (novel_id, author, is_completed, novel_description, novel_image, title) VALUES (6, '박수정(방울마마)', 0, '이래저래 인생 갑갑한 열여덟 살 여고생 미사. 
수학여행에서 교통사고를 당하고 눈을 떠 보니 십 년이 흘러 있다! 
“제가 스물여덟 살이라고요?” 
그리고 홀연히 나타난 미모의 남자가 날리는 충격적인 결정타. 
“ 내가 네 남편이야.” 
맙소사! ……잠깐, 근데 이거 개이득 아니야?
천진난만한 아내 미사와 비밀을 간직한 남편 윤하. 그들의 위험한 신혼은?!', 'https://comicthumb-phinf.pstatic.net/20160104_43/pocket_1451884589985ToRmk_JPEG/bigcover.jpg?type=m260', '위험한 신혼부부');
INSERT INTO websoso_db.novel (novel_id, author, is_completed, novel_description, novel_image, title) VALUES (7, '안테', 0, '가로등 불빛이 꺼지던 밤, 어둠에 스며든 낮은 목소리가 들려온다. 

“저기요.” 

고개를 돌린 세아의 시야를 맹렬히 긁으며 나타난 남자의 얼굴. 

10년 전, 죽은 줄로만 알았던 사랑하는 남자가 살아 돌아왔다. 

안겨오는 거대한 몸, 남자로 변한 체향. 

“누나 집에 들여 보내줘.” 

자신을 찾아온 열다섯 소년이 아닌 스물다섯 살의 도현과 만나게 된 세아는 떨어져 있던 시간 동안 달라진 도현에게 숨겨진 비밀을 하나씩 알게 되는데…….', 'https://comicthumb-phinf.pstatic.net/20150805_225/pocket_1438765168534ppRRW_JPEG/cover.jpg?type=m260', '너에게로 중독');
INSERT INTO websoso_db.novel (novel_id, author, is_completed, novel_description, novel_image, title) VALUES (8, '플아다', 0, '취직 하루 만에 쫓겨날 위기에 처한 입주가정교사 김이새. 
“앞으로 세 번, 사장님이 경고를 하시면 스스로 나가도록 하겠습니다.” 
경고 세 번? 웃기네. 
고용주 지원은 그렇게 이새를 얕잡아 보는데, 생각만큼 그녀가 순순히 물러나질 않는다. 
급기야, 넓은 집 어두운 공간에서 고용주와 고용인은 은밀하게 꽁냥거리기 시작하는데…….
갑질 가득한 집에서 을질 제대로 하시는 발랄 김이새 선생의 얼음 저택 생존기! 
자, 이제 게임을 시작하지.', 'https://comicthumb-phinf.pstatic.net/20160224_223/pocket_1456276702496ObSHY_JPEG/image.JPEG?type=m260', '가르쳐 주세요');
INSERT INTO websoso_db.novel (novel_id, author, is_completed, novel_description, novel_image, title) VALUES (9, '안테', 0, '전생에 나비였던 기질을 타고나 호접몽(胡蝶夢)에 빠진 지혜는 한번 잠들면 48시간을 일어나지 못한 채 꿈속에서 헤매게 된다. 

그런 지혜에게 무당은 두 날개를 잡아 먹어줄 거미를 만나야 정상적으로 살 수 있다고 하지만, 

문제는 지혜가 그 상대를 만나면 거부감을 느끼는 것. 

한편 완벽한 남자로 대중들의 사랑을 받는 기업인 진원은 저를 볼 때마다 소스라치게 놀라는 지혜가 기분 나쁘면서도 왜인지 자꾸 끌리게 되는데……. 

거미처럼 은밀히 본성을 숨긴 남자와 그를 피하려는 여자의 아찔한 로맨스.', 'https://comicthumb-phinf.pstatic.net/20160729_86/pocket_1469770898359jT0dO_JPEG/_03.jpg?type=m260', '나쁜 관계');
INSERT INTO websoso_db.novel (novel_id, author, is_completed, novel_description, novel_image, title) VALUES (10, '박수정(방울마마)', 0, '입사 3년차에 아직도 사무실 막내인 유림. 
그런데 그토록 기다려 온 신입사원 승현은, 하필이면 회장님 친손자였다! 
“야, 이 미친 새끼야!” 
참고 참던 유림이 드디어 폭발한 이유는? 
나이키와 아디다스가 명품인 뼛속까지 체육녀와 하이엔드 패션을 추구하는 미모의 연하남의 티격태격 오피스 로맨틱 코미디!', 'https://comicthumb-phinf.pstatic.net/20141202_21/pocket_14174834999609rWVE_JPEG/%C0%A7%C7%E8%C7%D1_%BD%C5%C0%D4%BB%E7%BF%F8-%C7%A5%C1%F6%28%C0%CE%C5%B8%C0%CC%C6%B2%29.jpg?type=m260', '위험한 신입사원');
INSERT INTO websoso_db.novel (novel_id, author, is_completed, novel_description, novel_image, title) VALUES (11, '안테', 0, '가슴 전문 스타 성형의사이자, 절대영도의 미남 현신.
그러나 그 정체는 인간의 ‘오만’을 흡수하는 대악마!
눈빛만으로 모두를 홀리는 마력의 이 남자,
의외로 ‘피’를 두려워한다는 약점이 있다.
인간계에서 의사로 살아가기에는 너무나 치명적인 체질.
그런데 어느 날 나타난 당돌한 여자, 이나에게는
현신의 유혹이 전혀 통하지 않을뿐더러,
그녀와 함께 있으면 피를 봐도 아무렇지도 않다.
이 여자, 곁에 두고 싶다. 계약을 맺어서라도!

“인간답게 너에게 한번 다가가 볼까. 
네가 날 좋아하게 된다면야, 이 모든 건 쉬워질 테니까.”', 'https://comicthumb-phinf.pstatic.net/20200527_51/pocket_159055793045644lJe_JPEG/-_-.jpg?type=m260', '악마라고 불러다오');
INSERT INTO websoso_db.novel (novel_id, author, is_completed, novel_description, novel_image, title) VALUES (12, '임혜', 0, '온 세상이 무겁게 가라앉은 깊은 밤. 흑암보다 어둡고 얼음보다 서늘한 우아한 짐승, 세류를 만났다. 운명처럼 시작된 그와의 만남은 생명을 위협하는 칼날과도 같았으나, 아무것도 가지지 못한 내 인생의 구원이었다. 그랬기에 슬픔을 예감하면서도 전부를 걸고 사랑할 수밖에 없었던 나의 이야기. 우아한 네 짐승과 함께하는 신비한 대협곡 속으로 여러분을 초대합니다.', 'https://comicthumb-phinf.pstatic.net/20140904_170/pocket_1409815752831tczB9_JPEG/1.jpg?type=m260', '우아한 짐승의 세계');
INSERT INTO websoso_db.novel (novel_id, author, is_completed, novel_description, novel_image, title) VALUES (13, '유세라', 0, '낮에는 저명한 범죄학자, 밤에는 수인들의 왕으로 군림하는 에릭 클리서링 경에게 인생을 저당 잡힌 소녀가장 이로윤.
속은 시커먼 호랑이지만 겉은 매력적인 신사인 에릭이 갑자기 로윤의 후견인을 자처하고 나서면서, 그녀의 주변에는 무시무시한 사건들이 끊이질 않는데…….
수인들의 힘을 악용해 세계를 집어삼키려는 마피아 조직의 위협 속에서 기묘한 계약으로 얽혀버린 두 사람의 섹시하고 미스터리한 로맨틱 판타지가 시작된다!', 'https://comicthumb-phinf.pstatic.net/20160405_194/pocket_14598210938028afcE_JPEG/cover.jpg?type=m260', '신사와의 기묘한 계약');
INSERT INTO websoso_db.novel (novel_id, author, is_completed, novel_description, novel_image, title) VALUES (14, '김레인', 0, '눈을 떠보니 난 마계 프렌시아의 마녀가 되어 있었다. 그것도 무려 순수혈통 마녀가 되어 버린 나는 깨어나자마자 이름도 얼굴도 모르는 마왕의 반려자로 간택받기 위해 왕궁으로 가야 했다. 목표는 안전귀가였지만 어째 마음대로 되는 것 같지가 않다. 마녀로서의 파란만장한 삶이 그렇게 시작되어 버렸다.', 'https://comicthumb-phinf.pstatic.net/20141107_38/pocket_1415341316357xRJTK_JPEG/1.jpg?type=m260', '프렌시아의 꽃');
INSERT INTO websoso_db.novel (novel_id, author, is_completed, novel_description, novel_image, title) VALUES (15, '손세희', 0, '열여섯 살 풋내기와의 결혼을 위해 디어뮈드 제국으로 온 모르웨이의 공주, 에스메랄다. 

하지만 약혼자는 정신병력이 있는 꼬마 폭군인 데다, 디어뮈드의 사교계는 그녀를 반기지 않는다. 

그때 그녀의 눈앞에 나타난, 그녀를 구해줄 수 있는 유일한 남자. 

“저를 혼자 두실 건가요?” 

약혼자가 있는 그녀에게 위험한 사랑이 시작된다.

무서운 음모와 야비한 암투가 도사리는 디어뮈드 황실에서 펼쳐지는 섹시 로맨스 판타지!', 'https://comicthumb-phinf.pstatic.net/20170126_169/pocket_1485399198250kFKkC_JPEG/__.jpg?type=m260', '두 입술 사이');
INSERT INTO websoso_db.novel (novel_id, author, is_completed, novel_description, novel_image, title) VALUES (16, '동화(桐華)', 0, '이 작품은 桐华의 소설 步步惊心(2006)을 한국어로 옮긴 것입니다.


SBS 드라마 <달의 연인- 보보경심 려> 원작소설!

18세기 초 청나라 강희제 시대로 시간을 거슬러 간 
21세기 중국 여성 장효의 사랑과 운명!

현대에서 평범한 이십대 직장인이었던 장효는 불의의 사고로 정신을 잃는다. 그녀가 다시 눈을 뜬 곳은 300여 년 전 청나라 강희 43년, 팔황자 윤사의 저택. 
그녀는 이제 팔황자의 처제이자 곧 궁녀가 될 운명인 열세 살 소녀 약희다. 

비정한 운명의 장난인 듯 황자들의 권력 다툼 한가운데에 휘말려드는 소녀 약희.
훗날 냉혹한 군주 옹정제로 군림할 사황자와 그의 숙적 팔황자. 
이 세 사람의 애절한 로맨스 <보보경심>. 

이 책은 2006년 중국에서 출간된 후 독자들의 입소문을 타고 그해의 베스트셀러로 선정되었고, 드라마 방영과 함께 개정판이 출간되며 구간 포함 120만 부의 판매고를 올린 화제의 밀리언셀러 소설이다. 

<보보경심>은 2011년 9월 호남위성TV에서 드라마화되며 일약 중국의 ‘국민드라마’로 엄청난 인기를 누렸다. <보보경심> 열풍은 중국을 넘어 대만, 싱가포르, 그리고 한국에 이르는 아시아 전역에서 큰 반향을 일으키고 있다.', 'https://comicthumb-phinf.pstatic.net/20171222_284/pocket_1513924928477zLimr_JPEG/246874_1.jpg?type=m260', '보보경심(步步驚心)');
INSERT INTO websoso_db.novel (novel_id, author, is_completed, novel_description, novel_image, title) VALUES (17, '유세라', 0, '인생은 단순하게 살아야 한다. 
말단공무원 강예나는 이 중요한 신조를 지키고 싶었건만, 어째 그녀의 인생은 반대로 굴러간다. 고약한 백작님이 갑자기 나타나더니 제국의 최고 엘리트 기관인 <황제 폐하 직속 특별수사국>으로 발령 내질 않나, 승진을 시키질 않나, 제국의 공적 1호인 용공과 전쟁까지 하질 않나. 정신 차리고 보니 백작님한테 질질 끌려가고 있다. 이젠 멈출 수 없는 이 기묘한 동행의 끝은 어디일까?', 'https://comicthumb-phinf.pstatic.net/20151216_64/pocket_1450242307054dFiU0_JPEG/cover.jpg?type=m260', '백작과의 기묘한 산책');
INSERT INTO websoso_db.novel (novel_id, author, is_completed, novel_description, novel_image, title) VALUES (18, '하야시', 0, '한국형 ‘트와일라잇’, 심장을 두드리는 뱀파이어 로맨스!

사라진 ‘뱀파이어 꽃’을 찾아 500년 만에 인간 세상에 나타난 뱀파이어, 루베르이. 그리고 어린아이의 모습을 한 뱀파이어와 계약을 하게 된 서영. 어두운 밤, 달빛 아래 검은 날개를 펼치고 있는 매력적인 남자가 자신과 계약을 맺은 ‘루이’라는 것을 알게 되면서 서영은 두 개의 모습을 한 뱀파이어에게 점점 빠져들어 가는데…….', 'https://comicthumb-phinf.pstatic.net/20140325_289/pocket_1395709946335cp667_JPEG/vamflower_0.jpg?type=m260', '뱀파이어의 꽃');
INSERT INTO websoso_db.novel (novel_id, author, is_completed, novel_description, novel_image, title) VALUES (19, '임혜', 0, '인어의 나라 해국. 해국의 다섯 왕 중 절대 미모를 자랑하는 태랑. 심장이 없이 태어난 그는 스물다섯 살이 되기 전, 저를 사랑하는 여인에게서 심장을 빼앗아야 살 수 있다. 그러나 여인과 닿을 수조차 없는 몸이라 죽을 날만 기다리던 그에게 솔루가 나타났다. 안을 수 있는 단 하나뿐인 여인. 심장을 가질 수 있는 유일한 기회를 놓칠 수 없었다. 하지만 이 여자 만만치 않다. 방실방실 웃으면서 넘어올 듯, 넘어오지 않는 솔루. 그녀에게서 심장을 취하기 위한 태랑의 유혹이 시작된다.', 'https://comicthumb-phinf.pstatic.net/20150611_25/pocket_1433998582335l7DzU_JPEG/1.jpg?type=m260', '괴물의 순결한 심장');
INSERT INTO websoso_db.novel (novel_id, author, is_completed, novel_description, novel_image, title) VALUES (20, '서이나', 0, '내 이웃집에 늑대가 산다. 정말 거짓말 안 하고 아주 위험하고 잘생긴 늑대가 살고 있다.
아프리카에서 시작된 기묘한 인연. 누구보다 뜨거웠고, 누구보다 강렬했다.
마치 한여름밤의 꿈 같았던 보름달이 떴던 그 밤.
하지만 바람처럼 사라진 그 사람. 조금 특별하긴 했지만 그뿐이라 생각했는데……
1년 후, 서울에서 지독한 운명으로 엮이게 될 줄은 정말 꿈에도 상상하지 못했다.
3개월 뒤, 누군가로 인해 죽을 운명인 그녀. 그녀를 살리기 위해 그렇게 조금 특별한 닥터가 찾아온다.', 'https://comicthumb-phinf.pstatic.net/20150403_69/pocket_1428023406573329tL_JPEG/cover.jpg?type=m260', '이웃집에 늑대가 산다');
INSERT INTO websoso_db.novel (novel_id, author, is_completed, novel_description, novel_image, title) VALUES (21, '남희성', 0, 'NPC한테 아부하여 밥을 얻어먹는 처세술의 대가.
주야장천 수련에 몰두하는 억척의 지존. 돈을 벌겠다는 집념으로 뭉친 주인공 위드에게 게임 속 세상은 모조리 돈으로 연결된다. 그런 그가 험난한 퀘스트를 수행한 대가로 얻은 것은 전혀 돈 안 될 것 같은 \'조각사\'라는 직업이었다. 그러나 위드 사전에 좌절이란 없다. \'전설의 달빛 조각사\'가 되어 떼돈을 벌기 위한 위드의 대장정이 시작된다! 

<하이마>, <태양왕>의 작가 남희성의 게임 판타지 장편소설 『달빛조각사』', 'https://comicthumb-phinf.pstatic.net/20161228_280/pocket_1482887340897zob42_JPEG/image.JPEG?type=m260', '달빛조각사');
INSERT INTO websoso_db.novel (novel_id, author, is_completed, novel_description, novel_image, title) VALUES (22, '청빙 최영진', 0, '네 미녀의 철벽 호위 아래, 삼국시대를 제패하라! 
순간기억능력을 가진 미소년, 진용운은 아버지가 남긴 신비한 유물의 힘으로 시공을 이동하게 된다. 
그가 도착한 곳은 소설과 게임으로만 접했던 삼국지의 시대. 
새로운 세상에 적응하기도 쉽지 않은데, ‘위원회’라는 강대한 적이 공격해 온다. 
용운은 절세 미녀 사천신녀와 의형제 조운 그리고 미래에 대한 지식으로 난세에 맞서 나간다.', 'https://comicthumb-phinf.pstatic.net/20171018_53/pocket_1508282625285CbRwT_JPEG/_4.jpg?type=m260', '삼국지 호접몽전');
INSERT INTO websoso_db.novel (novel_id, author, is_completed, novel_description, novel_image, title) VALUES (23, '가우리', 0, '강철의 열제 2부 - 서울정벌기 


내 본신이 깃들어 있는 곳으로 돌아가기 위해 피비린내 나는 길을 걸어왔다. 
죽음보다 더한 그리움의 고통 그리고 그 끝에 갈 수 있다는 희망. 
하지만 하늘은 그것을 허락하지 않았다! 

또 다른 세계로의 이동, 도착한 곳은 바로 서울! 
이계를 호령한 가우리의 열제, 고진천. 
이제 그가 대한민국을 진동시킨다! 

<작가 소개>  
필명 가우리  
본명 임동원  

2010 ~ 現작가집단 은자림 대표  
2010 ~ 現한국판타지스토리텔링 협회 이사  
2010 ~ 現게임114 스토리텔링팀 총괄  

* 주요 작품 : 강철의 열제, 폭풍의 제왕, 무위투쟁록, 시티 블레이더(카발2 웹툰 시나리오)', 'https://comicthumb-phinf.pstatic.net/20180502_19/pocket_1525232423990KOJva_JPEG/2.jpg?type=m260', '강철의 열제 2부 - 서울 정벌기');
INSERT INTO websoso_db.novel (novel_id, author, is_completed, novel_description, novel_image, title) VALUES (24, '임경배', 0, '왕의 심장이 불타 사라질 때, 현세의 운명을 초월한 존재가 이 땅에 강림하리라! 
폭군을 쓰러트리고 이세계를 구원한 지구인 소년 성시한. 
부와 명예, 아름다운 연인… 해피엔딩으로 이야기는 끝인 줄 알았건만, 그 대가는 목숨 걸고 이룩한 모든 걸 빼앗기고 지구로 추방되는 것이었다. 
이에 시한은 10년의 절치부심 끝에 테라노어로 되돌아오게 되는데… 한 번 세상을 구한 영웅의 이계 ‘재’진입 이야기!

* 이 작품은 \'네이버 웹소설\'(novel.naver.com)에서 연재중인 작품입니다.    
   웹소설 연재분은 무료로 즐기고, 다음 화를 미리 보고싶다면 네이버북스에서 구매하여 이용하세요.', 'https://comicthumb-phinf.pstatic.net/20160822_152/pocket_1471849279299JgBck_JPEG/___.jpg?type=m260', '이계진입 리로디드');
INSERT INTO websoso_db.novel (novel_id, author, is_completed, novel_description, novel_image, title) VALUES (25, '박성호', 0, '평범한 고등학생이던 이결. 
어느 날 레드 드래곤 레이어드의 마법으로 인해 아르세니아 대륙으로 소환되었다. 
원래 세계로 돌아가기 위해서는 어딘가에 흩어져 있는 드래곤 하트 조각이 필요한데…….
드래곤 하트를 찾아 떠나는 고등학생 이결과 하프엘프 소녀 루시의 아르세니아 대륙 모험기!', 'https://comicthumb-phinf.pstatic.net/20140829_263/pocket_1409279377659P3UEu_JPEG/intitle.jpg?type=m260', '아르세니아의 마법사');
INSERT INTO websoso_db.novel (novel_id, author, is_completed, novel_description, novel_image, title) VALUES (26, '무장', 0, '불가사의한 감각과 탁월한 전투 능력의 소유자, 강찬!

그런데 심장이 뻑뻑할 정도로 엄습해 오는 이 불안함은 도대체 뭐란 말이냐!

 

퍼억!

둔탁한 소리가 들리며 세상이 온통 하얗게 변했다.

 

애써 정신을 차린 뒤, 벽에 걸린 거울에 얼굴을 비치자

그곳에는 웬 허약한 고등학생의 모습이 보였다.

게다가 주변에는 온통 자신을 괴롭히는 놈들뿐…….

 

‘다 죽여 주마.’

 

갓 오브 블랙필드!

모르나 본데, 이건 적군이 만들어 낸 말이다.

죽음을 선사하는 신이라는 뜻이지!', 'https://comicthumb-phinf.pstatic.net/20230206_112/pocket_1675662902816mnrOT_JPEG/178856_419.jpg?type=m260', '갓 오브 블랙필드');
INSERT INTO websoso_db.novel (novel_id, author, is_completed, novel_description, novel_image, title) VALUES (27, '윤현승', 0, '윤현승 작가의 대표작, <하얀 늑대들> 이북 최초 공개!
기존 원작을 작가님께서 직접 다시 쓰신 개정판으로 선보입니다. 

전쟁터에서 패잔병이 된 농부 카셀은 우연히 아란티아의 보검을 주워,
그 주인인 하얀 늑대들을 찾아 모험을 떠난다. 
하지만 막상 만난 하얀 늑대들은 거꾸로 카셀에게 캡틴을 하라고 떠넘기는데...

농부에서 패잔병으로, 패잔병에서 캡틴으로,
살아남기 위한 거짓말에서 친구들을 지키려는 희생으로,
지금 카셀의 싸움이 시작된다.

* 네이버 웹소설에서 <하얀 늑대들 외전>이 연재중입니다.', 'https://comicthumb-phinf.pstatic.net/20141202_145/pocket_1417505677082CNeMJ_JPEG/ww0.jpg?type=m260', '하얀 늑대들');
INSERT INTO websoso_db.novel (novel_id, author, is_completed, novel_description, novel_image, title) VALUES (28, '청빙 최영진', 0, '하루아침에 모든 것이 RPG 게임처럼 바뀌었다. 
휴대폰을 쓰려면 레벨5, 인터넷을 하려면 레벨7이 되어야 가능한 세상. 
몬스터를 잡으면 진짜 아이템이 떨어지고, 반대로 당하면 석상이 되어 버린다. 

원인도 해결책도 모른다. 
사랑하는 이들을 구하기 위해 게임 재능을 활용하여 클리어 해나갈 뿐. 
\'프로젝트 J\'라 명명될 이 게임의 최종 스테이지는 과연 어디일까.', 'https://comicthumb-phinf.pstatic.net/20160912_227/pocket_147366163434224bz5_JPEG/j2.jpg?type=m260', '프로젝트J');
INSERT INTO websoso_db.novel (novel_id, author, is_completed, novel_description, novel_image, title) VALUES (29, '가우리', 0, '강철의 열제 

이 땅에 가장 영광된 이름, 가우리. 
그 이름을 지키고 세운 그를 강철의 열제라 부른다! 
이계의 대륙을 뒤흔드는 고구려의 웅혼들. 
이루지 못한 꿈을 향한 삼족오의 질주. 
분노와 아쉬움의 한숨 섞인 역사가 새롭게 다시 쓰인다! 
지키지 못한 역사는 더 이상 우리 것이 아니다! 
들리는가! 묵빛의 찰갑 소리. 보이는가! 붉은색의 삼족오 깃발. 
위대한 역사의 시작, 그 중심에 그대가 있다! 


<작가 소개> 
필명 가우리 
본명 임동원 

2010 ~ 現작가집단 은자림 대표 
2010 ~ 現한국판타지스토리텔링 협회 이사 
2010 ~ 現게임114 스토리텔링팀 총괄 

* 주요 작품 : 강철의 열제, 폭풍의 제왕, 무위투쟁록, 시티 블레이더(카발2 웹툰 시나리오)', 'https://comicthumb-phinf.pstatic.net/20140411_59/pocket_1397216701980wiqJS_JPEG/%B0%AD%C3%B6%C0%C7_%BF%AD%C1%A6_%B5%F0%C4%DC%BA%CF_CI_21_%BF%CF.jpg?type=m260', '강철의 열제');
INSERT INTO websoso_db.novel (novel_id, author, is_completed, novel_description, novel_image, title) VALUES (30, '박새날', 0, '박새날 게임 판타지 장편소설 『템빨』 공사장에서 벽돌 나르고 삽질하는 불운한 인생의 신영우.
 그런데 심지어 게임 속에서 노가다라니. 하지만 불운한 인생이라 한탄하던 그에게도 행운이 찾아오는 것인가. 퀘스트 수행을 위하여 북쪽 끝의 동굴로 향한 \'그리드\'. 그곳에서 \'파그마의 기서\'를 발견한 그는 레전드리 직업으로 전직하게 되는데…….', 'https://comicthumb-phinf.pstatic.net/20230823_152/pocket_1692776236083k3tj9_JPEG/187703_1903.jpg?type=m260', '템빨');
INSERT INTO websoso_db.novel (novel_id, author, is_completed, novel_description, novel_image, title) VALUES (31, '조석호', 0, '2015년 최고의 선택!

삶과 죽음이 교차한 그 순간!
평범한 인턴의 운명이 송두리째 바뀌었다.

천재 흉부외과의 최태수가 간다!', 'https://comicthumb-phinf.pstatic.net/20191227_51/pocket_1577423575207mWiIR_JPEG/__300.jpg?type=m260', '닥터 최태수');
INSERT INTO websoso_db.novel (novel_id, author, is_completed, novel_description, novel_image, title) VALUES (32, 'ALLA', 0, '신의 심심함을 만족시키기 위해 시작된 변화.
그로 인해 멸망한 인류를 되살리기 위해 되돌아온 강 한수의 일대기.', 'https://comicthumb-phinf.pstatic.net/20231228_292/pocket_1703748067437h1FJO_JPEG/230685.jpg?type=m260', '환생좌');
INSERT INTO websoso_db.novel (novel_id, author, is_completed, novel_description, novel_image, title) VALUES (33, '김강현', 0, '개운하게 눈을 뜬 어느 날, 하루치의 기억이 몽땅 사라진 사실을 안 강진우. 잃어버린 하루 동안 자신이 벌인 일 때문에 여러가지 일을 겪는다. 자신에게 벌어진 일에 대해 차근차근 알아가던 중, 자신이 잠든 사이에 또 다른 자신이 남긴 동영상을 발견하는데…….', 'https://comicthumb-phinf.pstatic.net/20150107_72/pocket_14206122886889G2fp_JPEG/%B8%B6%BD%C5%C0%FC%BC%B3_%C7%A5%C1%F6_title.jpg?type=m260', '마신전설');
INSERT INTO websoso_db.novel (novel_id, author, is_completed, novel_description, novel_image, title) VALUES (34, '수수림', 0, '1993년 2월 첫째 주 월요일. 
김지훈이 의사로서의 첫 발을 디뎠다. 
그의 목표는 단 하나.
최고의 외과 의사가 되는 것이다.', 'https://comicthumb-phinf.pstatic.net/20200623_114/pocket_1592891438288EvdY8_JPEG/--2.jpg?type=m260', '그레이트 써전');
INSERT INTO websoso_db.novel (novel_id, author, is_completed, novel_description, novel_image, title) VALUES (35, '관희천', 0, '어렸을 적 치명적인 부상으로 필드를 떠난,
축구신동 백진규.
그 후 학업과 치료에 전념하며 
평범한 삶을 산다.
그러다 대학 진학 후 유학길에 우연히 
축구를 다시 접하게 되는데…….', 'https://comicthumb-phinf.pstatic.net/20150608_146/pocket_1433724020337lTYJd_JPEG/image.JPEG?type=m260', '필드');
INSERT INTO websoso_db.novel (novel_id, author, is_completed, novel_description, novel_image, title) VALUES (36, '독고진', 0, '폭발적인 스피드, 강철 같은 체력을 바탕으로 뿜어져 나오는 파워, 맹수와도 같은 감각!
모든 것을 가진 최고의 유망주 차지혁!
찬란한 미래를 꿈꾸었지만, 모든 것을 잃었다.
믿고 따랐던 이들에게 버림 받고.
함께 웃고 울었던 이들에게 배신 당했다.
사는 게 지옥 같았던 그에게 찾아온 기적과도 같은 일!

 [월드 사커 온라인에 오신 걸 환영합니다.]
 [플레이어 포지션을 선택하십시오.]

이제는 더 이상 당하고 살지 않는다!

 *스트라이커(striker)!
축구 경기에서 득점력이 가장 뛰어난 선수!', 'https://comicthumb-phinf.pstatic.net/20160524_22/pocket_1464075398570Rf7K5_JPEG/_.jpg?type=m260', '스트라이커');
INSERT INTO websoso_db.novel (novel_id, author, is_completed, novel_description, novel_image, title) VALUES (37, '자카예프', 0, '통쾌함의 끝은 과연 어디인지.
함께 여행을 떠나볼까요?

이 세상에 넘쳐나는 나쁜 놈들을 향한 통렬한 한 판!', 'https://comicthumb-phinf.pstatic.net/20170428_257/pocket_1493351005911b5osR_JPEG/0dldlf.jpg?type=m260', '이것이 법이다');
INSERT INTO websoso_db.novel (novel_id, author, is_completed, novel_description, novel_image, title) VALUES (38, '실탄', 0, '재벌가의 사생아, 차효주. 수천 년 뒤 미래에서 온 에릭을 만나 모든 것이 변한다. 에릭은 우주선 수리를 위해 그녀의 도움을 받고, 그녀는 미래의 발전 문물을 이용해 자신을 버린 모든 이들에게 복수한다.', 'https://comicthumb-phinf.pstatic.net/20160805_300/pocket_1470383063942mF3hL_JPEG/cover.jpg?type=m260', '야만의 지구');
INSERT INTO websoso_db.novel (novel_id, author, is_completed, novel_description, novel_image, title) VALUES (39, '장우산', 0, '한 치 앞도 내다볼 수 없는 연예계.
그곳에 첫 발을 내딛은 날, 내 눈에 미래가 보이기 시작했다.', 'https://comicthumb-phinf.pstatic.net/20170202_188/pocket_1486019445462yL0qA_JPEG/-.jpg?type=m260', '탑 매니지먼트');
INSERT INTO websoso_db.novel (novel_id, author, is_completed, novel_description, novel_image, title) VALUES (40, '천지무천', 0, '주식 투자에 실패해 나락으로 빠진 강태수.
그런데, 눈을 떠보니 22년 전 과거로 돌아왔다?
 
“다시는 후회하는 삶을 살지 않으리라!”
 
미래의 지식과 달라진 머리는 그를 천재적 사업가로 만들었고, 
지난 삶에 대한 깊은 후회는 그를 혁명가로 이끌었다.
부패한 기업인, 무능한 정치가, 희생당하는 서민들과 IMF…
그리고 또다시 다가오는 어두운 그림자들…….
지나간 역사를 바로잡지는 못하지만 비뚤어져 가는 미래는 바꾸리라.
 
새로운 삶을 살게 된 강태수. 변혁의 중심에 서다!', 'https://comicthumb-phinf.pstatic.net/20220511_263/pocket_1652238814602EahzM_JPEG/166900.jpg?type=m260', '변혁1990');
INSERT INTO websoso_db.novel (novel_id, author, is_completed, novel_description, novel_image, title) VALUES (41, '현임', 0, '최초로 흑도를 통합한 흑도대종사, 흑암제 연호정.
삼교의 난(亂)으로 정파와 손을 잡고 그들을 물리치지만, 무림맹주의 계략에 휘말려 한스러운 삶을 마감하는데.

"....진짜 내 집이다!"

눈을 떠 보니 무림 최고 명문가이자 과거 멸문했던 연가(燕家),
한시도 잊은 적 없던 그의 집에 와 있었다.

"이번만큼은 실수하지 않아. 절대로."

가문의 멸문을 막고, 훗날 창궐할 삼교(三敎)의 난을 막기 위해 질주한다! 대공자 연호정의 고군분투 무림 통합기.', 'https://dn-img-page.kakao.com/download/resource?kid=6j3IV/hzVqFxN94R/3hfSbYokWQ040EktRuFEz1&filename=th3', '흑백무제');
INSERT INTO websoso_db.novel (novel_id, author, is_completed, novel_description, novel_image, title) VALUES (42, '조진행', 0, '[독점연재]

"꽃으로도 때리지 마라. 그가 구천 하늘의 주인이다."

서자로 태어나 큰엄마와 배다른 형제자매들에게 갖은 폭력과 괴롭힘을 당하며 살아온 여섯 살 연적하.
결국 부친마저 병으로 죽자 큰엄마는 연적하를 창고에 가두어 버리는데……

세상과 격리되어 창고에 갇힌 십 년.
인외(人外)의 무공을 얻어 세상으로 탈출하다!

"내가 연씨들에게 감정이 좀 많아."

구천구검의 오롯한 전승자 연적하의 거침없는 강호행이 지금 시작된다!

표지 일러스트 : 반경', 'https://dn-img-page.kakao.com/download/resource?kid=bJvZCK/hyqFdloLXQ/Cux7NbBvYMpQP7dWzkbOh0&filename=th3', '구천구검');
INSERT INTO websoso_db.novel (novel_id, author, is_completed, novel_description, novel_image, title) VALUES (43, '태선', 0, '[독점연재]

고아였지만 지독한 노력 끝에 외과 의사가 된 진천희.
그런 그가 자신이 읽던 소설속 무림 세계로 빙의 했다!

"그래도 내가 의사여서 다행이다. 여기서 의원 노릇 하면서 살면 되겠네."

그렇게 안도하던 그의 앞에 나타난 것은 빈사 상태가 된 소설 속 세계의 주인공인 천마 여하륜.

"야, 살려준 은혜가 있으니 형이라 불러라."

천마에게 형이라 불리며 강호제일신의가 되는 그의 일대기.', 'https://dn-img-page.kakao.com/download/resource?kid=EGBun/hzXauIPPq9/0249VjLa0dcWT3o4RTiug1&filename=th3', '의원, 다시 살다');
INSERT INTO websoso_db.novel (novel_id, author, is_completed, novel_description, novel_image, title) VALUES (44, '사도연', 0, '소천마(少天魔) 연운휘(燕雲輝).
단전 없이 태어났지만 사술과 인성질… 아니, 이능만으로 천마신교 소교주가 된 존재.
그를 가리키는 별명은 많았다.

―사술의 대법사.
―구천을 떠도는 망령들의 왕.

반면에, 그에 대한 평가는 아주 간단했다.

―그가 웃으면 당장 뒤도 돌아보지 말고 튀어라. 그건 그냥 네가 엿된다는 뜻이다.
―그의 눈 안에 들어라. 그럼 평생 우산이 되어줄 것이니. 단, 그게 가능하다면.

그렇게 파란만장하게 살던 그가 부교주와 장로들의 반란에 직면하고 말았다.

"날 죽일 때 죽이더라도 내가 어떤 놈인지는 떠올려야 하셨소, 사숙. 그러니까 다 뒈져라."

하지만 그는 초대 천마의 성물인 마룡검을 폭발시켜 반란자들과 함께 장렬히 산화하고,
전혀 생각지도 못한 장소에서 눈을 뜨게 되는데…….

"당문(唐門)? 그것도 분가라고?"

사천당문 구룡분가의 서자로 깨어난 당운휘.
과연 그는 사부님을 되찾고 흩어진 신도들을 모아 천마신교를 재건할 수 있을까?', 'https://dn-img-page.kakao.com/download/resource?kid=BCQy5/hAdNViOykP/jjODI5Ncure37EbrOVqCx0&filename=th3', '환생마신전');
INSERT INTO websoso_db.novel (novel_id, author, is_completed, novel_description, novel_image, title) VALUES (45, '사야객', 0, '천마신교의 교주이자, 천하제일인이라 불리는 절대천마.
영생불멸군림대법으로 인해 정파에서 깨어나고 마는데…….

"뭐? 화산파? 이건 또 무슨 개소리야?"

갑자기 화산파의 제자가 된 마도 대종사의 이야기.
몰락의 길을 걷던 화산파가 다시 비상하기 시작한다.', 'https://dn-img-page.kakao.com/download/resource?kid=BNZhR/hzVqNwBrHt/6elSWXIyFujGqf30KadKO0&filename=th3', '화산천마');
INSERT INTO websoso_db.novel (novel_id, author, is_completed, novel_description, novel_image, title) VALUES (46, '정천', 0, '독사라 불리는 전문 싸움꾼, 방무진.
훗날, 하오문주가 되어 죽을 운명이었으나, 죽을 위기를 넘기며 운명이 뒤틀렸다.
검신이 된 그는 한 가지 결심을 하게 되는데.

“하오문을 천하제일문으로 만들겠어!”

독사 방무진이 무림을 강타한다.', 'https://dn-img-page.kakao.com/download/resource?kid=bCNSsQ/hzXarrGMRJ/YGhcXoigsEIHUTjNruY0N1&filename=th3', '하오문의 절대검신');
INSERT INTO websoso_db.novel (novel_id, author, is_completed, novel_description, novel_image, title) VALUES (47, '향란', 0, '『은해상단 막내아들』

천하제일의 상재를 타고난 은서호
승승장구하던 그를 가로막는 자들

“어째서 무림맹이 나를…….”
“너무 크게 성장해서 귀찮아졌거든, 그러니까 눈에 거슬린다는 거지.”

상단 일을 시작했던 그날로 돌아왔고, 굳게 다짐한다
이번 생에서는 절대 후회하지 않기로

“그렇게 네놈들이 깔본 돈으로 무너뜨려 주마.”

천재적인 두뇌와 뛰어난 무공 재능까지
역사에 남을 위대한 상황(商皇)의 행보가 시작된다!', 'https://dn-img-page.kakao.com/download/resource?kid=uJNFh/hAd48PCVfg/jy7GHx3ga0ZuIKRzWmQAhK&filename=th3', '은해상단 막내아들');
INSERT INTO websoso_db.novel (novel_id, author, is_completed, novel_description, novel_image, title) VALUES (48, '열곰', 0, '남궁세가에서 쫓겨나 마교로 갔더니 마교에서도 배신당한 남궁천.
죽음을 앞둔 상태에서 성화령을 먹고 삶을 다시 살 기회를 얻다.

“그런데 왜 손에서 마공이 나가냐고오오오.”
- 유유유유융 -

졸지에 남궁세가 내의 마교 분자가 되었지만, 다행히도 성화령 융융이는 필요할 때만 도움을 준다. 이것도 기회라면 기회.
전생에는 뒷배가 없어 가문에서 쫓겨났지만….

“내가 어! 왕년에 천마 직계 제자도 해보고! 천마하고 겸상도 하고 그러던 사람이야!”', 'https://dn-img-page.kakao.com/download/resource?kid=b040CZ/hAdNTytKUf/mAmE5oe8YQxhgzn4xJKMmk&filename=th3', '남궁세가 망나니는 천마의 제자였다');
INSERT INTO websoso_db.novel (novel_id, author, is_completed, novel_description, novel_image, title) VALUES (49, '제로빅', 0, '[독점연재]

F급 헌터로 살았다. F급 헌터로 죽을 줄 알았다.

“어때, 쓸 만해?”
“이딴 쓰레기는 어디서 주워왔냐?”

집 앞 분리수거장에서 고물 캡슐을 발견하기 전까지는.

-[진태경]이 기기에 등록됩니다.
-[무림]에 접속하시겠습니까?

그리고 눈앞에 펼쳐진 새로운 세상.
살아 숨 쉬는 오감! 엄청난 자유도!

“와, 이런 건 처음 보네.”

삐빅.

-[로그아웃]이 불가능합니다!

“……이런 건 진짜 처음 보네.”

이거, 게임이 맞긴 한 거지?', 'https://dn-img-page.kakao.com/download/resource?kid=cvJVBQ/hyZ8wDeMjs/w84BZIMRWTLvy1eHBirTZk&filename=th3', '로그인 무림');
INSERT INTO websoso_db.novel (novel_id, author, is_completed, novel_description, novel_image, title) VALUES (50, '천봉', 0, '무협의 대가, 천봉
그가 써 내리는 새로운 무협!

「북천전기」

백야벌(白夜閥)의 팔대가문 중
북부무림을 지배하는 철혈가(鐵血家)의 차남, 이연후
그는 위대한 가문의 적자로 태어났으나
후계자인 형에게 걸림돌이 된다는 이유로 버림받고 쫓겨나는데……

“지켜보십시오. 아버지가 내쳤던 아들이
만들어 갈 철혈가의 세상을…….”

잊혀진 적자의 귀환,
감히 누구도 맞서지 못할 위대한 군주의 탄생,
그가 이끄는 북천(北天)의 포효에 천하가 진동한다!', 'https://dn-img-page.kakao.com/download/resource?kid=JULvC/hzCs7F6UT6/kKEz8khfvjC0m4KLHLDfZk&filename=th3', '북천전기');
INSERT INTO websoso_db.novel (novel_id, author, is_completed, novel_description, novel_image, title) VALUES (51, '소년행', 0, '<폐왕성>,<무인 이랑>, <무인행>에 이은
소년행 작가님의 신작 <선인지로> 연재 시작!

어려서부터 공부하여 열다섯에 무예의 극의를 깨우친 \'달천\'.
스승으로부터 하산하여 세상에 나가다!

“노니 뭐 하겠는가. 차라리 학당을 하나 차리면 어떻겠는가?”
“학당이요?”
“서당에서 동네 아이들 모아 놓고 천자문 가르치듯이 말일세.”

소년 달천은 세상을 배우며 익혀나가고,
동시에 선인이 가는 길, 선인이 사는 방법을 가르친다!

선인 달천, 후인을 양성하여 조선의 환란에 대비하라!', 'https://dn-img-page.kakao.com/download/resource?kid=cjMSU/hAd4qCYSg7/pbu2YgoKUaq7JxgQ1mEU90&filename=th3', '선인지로');
INSERT INTO websoso_db.novel (novel_id, author, is_completed, novel_description, novel_image, title) VALUES (52, '소년행', 0, '<폐왕성>의 김재명, <무인 이랑> 한이랑 이전에 무인 정성진이 있었다!
더욱 새롭게 돌아온 소년행 무예 소설의 초석 <무인행>


봄의 끝 무렵, 새로운 절도사가 부임한다는 소문이 돌기 시작했다.

“항상 똑같은 사람이 오는 모양이야. 이번에도 그렇지?”
“그래, 똑같은 놈들이지 뭐. 하나같이 말이야.”

동북 국경을 지키는 절도사로 부임한 정성진.
그에게는 말로는 설명하기 힘든 무(武)의 기운이 느껴졌다.

무인 정성진, 여진족을 물리치고 조선을 호령하라!', 'https://dn-img-page.kakao.com/download/resource?kid=BZ5fe/hzHNxNMr9e/Yd9zQTbiXMudkjNkEbred1&filename=th3', '무인행');
INSERT INTO websoso_db.novel (novel_id, author, is_completed, novel_description, novel_image, title) VALUES (53, '소년행', 0, '조선 북방군.
변함없는 이치를 따라 살다가 목숨이 다하는 날을 기꺼이 맞고자 한다.

조선 초기, 가장 강력했던 군대인 북방군.
소년이 입대해서 오장이 되고 대정까지 되었다.
청년 필한의 북방 정벌기!


*
다급하게 달려오던 놈의 목에 가져다 대는 정도에 불과하지만, 정확히 겨냥한 검격에 놈이 힘없이 무너졌다.
\'하나.\'

한의 검이 쓰러진 놈의 옆구리를 찍었다. 아악 하는 비명성이 들렸다.
\'둘.\'

놈이 몸을 비틀면서 쓰러졌지만, 빗장 한 개도 같이 떨어졌다.
\'셋.\'

필한을 베려던 놈의 목이 단칼에 날아가더니 바닥에 떨어졌다.', 'https://dn-img-page.kakao.com/download/resource?kid=F5KzH/hzwBljo7XX/uViBMKofemRBjmf7o6x6ok&filename=th3', '북정기');
INSERT INTO websoso_db.novel (novel_id, author, is_completed, novel_description, novel_image, title) VALUES (54, '이원호', 0, '<영웅시대 1부, 2부>

흙수저 이광의 인생 개척사.

군 시절부터 복학생시절, 취업과 생존경쟁,목숨을 걸고 나선 치열한 삶의 전장.
이것은 흙수저의 피비린내 나는 인생사이며 성공사이다.

실화를 기반으로 버무린 인간들의 생존사인 것이다.
이 시대를 거쳐간 세대는 모두 영웅이었다.
우리는 이 영웅들이 다져놓은 기반을 딛고 이렇게 사는 것이다.

이 이야기는 이 시대가 끝날때까지 계속된다.


<영웅시대 3부>

왜란(倭亂)
기록에 남지않은 영웅시대다.

무능하고 비겁한 왕(王)
당쟁으로 왜군의 침략을 무시한 탐관오리.
수만명 군사를 사지(死地)로 몰아넣고 도망친 대장군.
도망쳤다가 부하가 공을 세우자 모함해서 죽이고 공을 가로챈 병마사.
양반의병 무리는 천민의병과 함께 싸우지 않겠다고 쫒아내었다.

이때 기록에 남지않은 영웅들이 없었다면 조선은 멸망했을 것이다

이것은 7년의 참혹한 전란(戰亂)을 헤쳐나간 영웅의 전설이다.', 'https://dn-img-page.kakao.com/download/resource?kid=DWxtH/hzwBmwj6tT/gjfbUQUOa3B3BrhnGxhPz1&filename=th3', '영웅시대');
INSERT INTO websoso_db.novel (novel_id, author, is_completed, novel_description, novel_image, title) VALUES (55, '소년행', 0, '<무인행>의 무인 정성진, <폐왕성>의 무사 김재명, 그리고 <만행무승>과 <산인 막둥이>의 소년 무인들까지!
그만의 무인들을 선보여온 소년행 작가의 새로운 무예 소설.

내 나이 십팔 세. 상방에서 호위를 구한다는 소식에 겁도 없이 배에 올라탔다.
호위라더니…… 칼받이 신세라고?!

배 위라 물러설 곳이 없다. 달아날 곳도 없다. 악착같이 싸워야 한다.
그냥 맞아 죽을 수는 없지 않은가?

고려 초기 혼란한 세상에서 강인한 기개와 비상한 머리로 거침없이 운명을 개척해나가는 청년 한이랑의 무협 활극!

***

서늘하고 뜨거운 느낌이 몸을 지지며 파고들었다. 스무 해가 채 되지 않는 살아온 시간이 주마등처럼 눈앞을 지나갔다. 튀어오르는 내 피가 붉다.
“악! 아악.”
단말마의 비명을 지르고 정신을 잃었다. 무언가 거대한 것이 바닥을 구르는 소리, 그리고 무수한 발걸음 소리가 멀어졌다.
어깨와 가슴 사이에서 흘러나오는 피가 진하다는 느낌이 마지막이었다.

‘어, 뭘까? 내가 살았어?’', 'https://dn-img-page.kakao.com/download/resource?kid=b6Zy0P/hzb7r4Zp32/EOBetbTKK1n9TfM06KDVS0&filename=th3', '무인 이랑');
INSERT INTO websoso_db.novel (novel_id, author, is_completed, novel_description, novel_image, title) VALUES (56, '문성실', 0, '10년의 침묵, 그리고 새로운 시작

\'신비소설 무\'는 1998년부터 하이텔, 나우누리, 유니텔 등 PC통신과 코넷, 네츠고, 한빛은행 인트라넷 등 각종 사이트에 동시 연재되면서 인터넷을 뜨겁게 달구었던 판타지 소설이다. 그때까지 널리 읽히던 서구의 판타지와 달리 우리나라 고유의 신화와 전설을 바탕으로 우리의 정서와 당대의 시대상을 담아냄으로써 한국형 판타지의 가능성을 보여주었다. \'신비소설 무\'가 보여준 가능성과 인기는 온라인상에서만 끝나지 않고 오프라인으로도 이어져 14권까지 발간되며 권을 거듭할수록 더 많은 독자의 사랑과 지지를 받았다.
하지만 작가의 재충전을 위한 잠깐의 휴식이 10여 년이나 이어지면서 많은 아쉬움과 안타까움을 남겼다. "신비소설 무\'를 사랑했던 독자들은 시리즈가 멈춘 지 10여 년이 지났어도 여전히 이 작품을 잊지 못하고 언제 완간되느냐고 문의하곤 했다. 독자들의 변함없는 사랑과 지지에 힘입은 작가는 마침내 오랜 침묵을 깨고 독자 곁으로 돌아왔다. 달라진 시대상에 걸맞게 업그레이드된 \'신비소설 무\'와 함께.
길어진 휴식기만큼이나 인간과 세상에 대한 작가의 이해가 더욱 깊고 따뜻해졌으며 그런 변화가 이야기 곳곳에 스며 있다. 그와 더불어 무속에 대한 해박한 지식과 애정까지 남다른 작가는 이 책에 마니아만 즐겨 읽는 판타지소설의 울타리를 뛰어넘어 인문학적 색채까지 담아내고 있다. 우리의 전통 신앙으로 민족과 희로애락을 함께해왔음에도 지금껏 백안시되었던 무속은 작가의 펜 끝에서 제 옷을 찾아 입고 우리만의 고유한 문화 콘텐츠로 새로운 생명력을 부여받는다.', 'https://dn-img-page.kakao.com/download/resource?kid=bPznhh/hybOVk3hk5/WcUwZnKfZZBidMDiuRWDYK&filename=th3', '신비소설 무(巫)');
INSERT INTO websoso_db.novel (novel_id, author, is_completed, novel_description, novel_image, title) VALUES (57, '소년행', 0, '굴하지 않는 용기를 지닌 소년 장무술이
검 하나로 펼치는 정의와 구도의 길,
<폐왕성>의 작가 소년행 신작 무예 소설 <만행무승>

“적지 않는다는 의미입니다. 무술(無述)입니다.”
조선 시대 광폭한 흉년의 시기, 처참한 가난으로 인해 산속 절 무이사에 맡겨진 소년 장무술. 부모가 일찌감치 세상을 떠난 후 이모의 손에 커오던 일곱 살의 아이는 이제 스님들과 함께 절간 생활을 시작하게 된다. 크고 작은 장난들로 절 안을 소란스럽게 만드는 무술을 눈여겨보던 큰스님은 원명 스님으로 하여금 무예를 가르치도록 하는데……

“무(武)로 도(道)를 깨치는 경지에 이를 수 있는 자질을 갖춘 아이입니다.”
무이사 근처 천애암에 머물던 무승(武僧) 원명은 무술에게 무예를 가르치면서 그의 능력이 범상치 않다는 사실을 눈치챈다. 그저 한 번 보는 것만으로도 모든 동작을 파악하는 이 어린 소년은 천애암에 있는 어떤 무인보다 훌륭하게 동작을 소화해내는 타고난 무골(武骨)이었다. 원명은 자애와 측은지심, 굴하지 않는 용기를 지닌 소년에게서 어떤 운명을 예감한다.

“이 검에는 나라님으로부터 받은 의무가 딸려 온다. 어떤 상황에서도 나라님을 위해 목숨을 초개처럼 바칠 사람이 받아야 한다.”
원명은 깊고 넓은 못이라는 뜻을 가진 희대의 명검 ‘생지(泩池)’를 무술에게 건넨다. 이는 태조가 내린 검으로 나라를 지키기 위해 쓰이도록 전대에 약조된 것이었다. 청년 무술은 이제 생지의 육 대 주인이 되어 정의와 구도의 길 위에 선다.', 'https://dn-img-page.kakao.com/download/resource?kid=zjOMt/hzb7zaVCSe/20jhJBWHSH40sufEpgFwj1&filename=th3', '만행무승');
INSERT INTO websoso_db.novel (novel_id, author, is_completed, novel_description, novel_image, title) VALUES (58, '소년행', 0, '"우리가 바라는 새로운 세상은..."
역사소설 <무인행>으로 사랑받은 작가 소년행이 새롭게 그리는 세상

고려 중기, 발해와 거란 금(여진)이 교체되던 혼란한 시대.
고려로 이주했던 고구려 \'조의선인\'의 후인들이 요동으로 귀환하고, 그 중심엔 중원의 천하제일인을 누르고 명실상부한 천하제일인으로 우뚝 선 무인 \'김재명\'이 있다.

이들의 요동 귀환을 막으려는 고려 귀족의 간계로 인해 김재명은 우연히 폐주된 의종의 구출작전에 참여하고, 그 대가로 선인들이 요동으로 귀환하게 된다. 김재명은 부친을 따라 북상하면서 여러 종족의 사람들을 포섭, 군인, 학자와 기술자, 신녀로 구성된 이들이 협력하면서 또다른 세상을 꿈꾸는데...

한편 요동은 금나라의 세상이 되어있고, 고구려의 후인들은 지리멸렬하여 존립의 기반을 세우기 조차 어렵다. 이에 김재명 일행은 과거 발해 동경성 인근에서 작은 성을 확보하는 것을 시작으로 인근을 병합하면서 세력을 키워나가고, 금나라와의 전쟁, 고려와 협력과 반목을 이어가며 옛 발해의 고토를 수복하게 된다.

새로운 세상을 꿈꾸는 무사 김재명, 그 여정의 끝엔 어떤 운명이 기다리고 있을 것인가.', 'https://dn-img-page.kakao.com/download/resource?kid=bUhYYN/hyfMWBk22t/dK5klQHTgKKyHQSqqsSn50&filename=th3', '폐왕성');
INSERT INTO websoso_db.novel (novel_id, author, is_completed, novel_description, novel_image, title) VALUES (59, '도저', 0, '하루 아침에 나는 강제로 인간이 아닌 병기(兵器)가 되었다.
재산, 회사, 가족, 육신까지 모든 걸 다 뺏어간 그 놈들을 잘게 씹어 먹어서라도 복수하겠다.

#액션 #스릴러 #복수 #SF #생명공학 #이능력 #서바이벌 #아포칼립스
.
.
.
40대, 탈모, 경력단절. 그리고 망가진 몸까지.
모든 걸 잃어버린 채 한강 다리 위.

"스탑, 스탑! 실험 딱 한번만 참여하면 400 드릴게요. 새로 시작하고 싶지 않으세요?"

그래. 이 간드러진 목소리.
내 복수는 바로 여기서부터였다.

굴지의 대기업 오성. 그리고 내 몸을 탐내는 자들.
이 씹어먹어도 모자랄 연놈들.

내 기꺼이 인간을 포기해주마.', 'https://dn-img-page.kakao.com/download/resource?kid=bzfejP/hzN2gsFqBo/ycUP6gZvsP1HkJ2TWWKhgK&filename=th3', '유전병기');
INSERT INTO websoso_db.novel (novel_id, author, is_completed, novel_description, novel_image, title) VALUES (60, '글쓰는 쿼카', 0, '#여주투톱 #여주판타지 #걸크러시 #오컬트 #동양풍 #워맨스 #현대판타지

"뱀이다!!"

승천 당일, 고작 한 인간의 외침으로 용이 되지 못하고 추락하고 만 이무기 이서.

천 년의 수련이 물거품이 된 울분과 분노를 참지 못해 산천을 불태우고, 수많은 인간을 죽인 죄로 지하동굴에 갇히게 된다.

오백 년 뒤, 지하동굴에서 탈출한 이서에게 저승신이 건넨 한 가지 제안.

“더도 말고 덜도 말고 네 분풀이로 애통하게 목숨을 잃은 이들의 수만큼 구제해 보아라.
그런다면 내 한시도 지체하지 않고 산산이 조각났던 네 여의주를 온전히 돌려주겠다.”

그 말과 함께 붙여준 사수는, 과거 이서의 승천을 방해한 인간의 후손이자 300년의 차사 경력을 자랑하는 무명.

“이런 풋내기를 제 사수로 붙이시겠다는 겁니까? 어디까지 저를 욕보이셔야 만족하시겠습니까?”
“오해가 있으신 것 같은데 저 또한 어르신의 사수가 되겠다는 제안에 동의한 적 없습니다.”
“감히 핏덩이 계집이 어느 안전이라고 함부로 입을 놀리는 게냐!”

애송이 사수와 신입 어르신의 최악의 첫 만남.

인간이라면 일단 죽이네 마네 하는 꼰대 수습을 교육하는 건지, 수발드는 건지, 육아하는 건지 모를 무명은 무사히 이서를 승천시킬 수 있을까?', 'https://dn-img-page.kakao.com/download/resource?kid=bI9oCx/hAdNPColhG/4NjOJHdKHUKI8gPytWnlLk&filename=th3', '원없이 성불시켜 드립니다!');

INSERT INTO websoso_db.novel_genre (genre_id, novel_genre_id, novel_id) VALUES (1, 1, 1);
INSERT INTO websoso_db.novel_genre (genre_id, novel_genre_id, novel_id) VALUES (1, 2, 2);
INSERT INTO websoso_db.novel_genre (genre_id, novel_genre_id, novel_id) VALUES (1, 3, 3);
INSERT INTO websoso_db.novel_genre (genre_id, novel_genre_id, novel_id) VALUES (1, 4, 4);
INSERT INTO websoso_db.novel_genre (genre_id, novel_genre_id, novel_id) VALUES (1, 5, 5);
INSERT INTO websoso_db.novel_genre (genre_id, novel_genre_id, novel_id) VALUES (1, 6, 6);
INSERT INTO websoso_db.novel_genre (genre_id, novel_genre_id, novel_id) VALUES (1, 7, 7);
INSERT INTO websoso_db.novel_genre (genre_id, novel_genre_id, novel_id) VALUES (1, 8, 8);
INSERT INTO websoso_db.novel_genre (genre_id, novel_genre_id, novel_id) VALUES (1, 9, 9);
INSERT INTO websoso_db.novel_genre (genre_id, novel_genre_id, novel_id) VALUES (1, 10, 10);
INSERT INTO websoso_db.novel_genre (genre_id, novel_genre_id, novel_id) VALUES (2, 11, 11);
INSERT INTO websoso_db.novel_genre (genre_id, novel_genre_id, novel_id) VALUES (2, 12, 12);
INSERT INTO websoso_db.novel_genre (genre_id, novel_genre_id, novel_id) VALUES (2, 13, 13);
INSERT INTO websoso_db.novel_genre (genre_id, novel_genre_id, novel_id) VALUES (2, 14, 14);
INSERT INTO websoso_db.novel_genre (genre_id, novel_genre_id, novel_id) VALUES (2, 15, 15);
INSERT INTO websoso_db.novel_genre (genre_id, novel_genre_id, novel_id) VALUES (2, 16, 16);
INSERT INTO websoso_db.novel_genre (genre_id, novel_genre_id, novel_id) VALUES (2, 17, 17);
INSERT INTO websoso_db.novel_genre (genre_id, novel_genre_id, novel_id) VALUES (2, 18, 18);
INSERT INTO websoso_db.novel_genre (genre_id, novel_genre_id, novel_id) VALUES (2, 19, 19);
INSERT INTO websoso_db.novel_genre (genre_id, novel_genre_id, novel_id) VALUES (2, 20, 20);
INSERT INTO websoso_db.novel_genre (genre_id, novel_genre_id, novel_id) VALUES (3, 21, 21);
INSERT INTO websoso_db.novel_genre (genre_id, novel_genre_id, novel_id) VALUES (3, 22, 22);
INSERT INTO websoso_db.novel_genre (genre_id, novel_genre_id, novel_id) VALUES (3, 23, 23);
INSERT INTO websoso_db.novel_genre (genre_id, novel_genre_id, novel_id) VALUES (3, 24, 24);
INSERT INTO websoso_db.novel_genre (genre_id, novel_genre_id, novel_id) VALUES (3, 25, 25);
INSERT INTO websoso_db.novel_genre (genre_id, novel_genre_id, novel_id) VALUES (3, 26, 26);
INSERT INTO websoso_db.novel_genre (genre_id, novel_genre_id, novel_id) VALUES (3, 27, 27);
INSERT INTO websoso_db.novel_genre (genre_id, novel_genre_id, novel_id) VALUES (3, 28, 28);
INSERT INTO websoso_db.novel_genre (genre_id, novel_genre_id, novel_id) VALUES (3, 29, 29);
INSERT INTO websoso_db.novel_genre (genre_id, novel_genre_id, novel_id) VALUES (3, 30, 30);
INSERT INTO websoso_db.novel_genre (genre_id, novel_genre_id, novel_id) VALUES (4, 31, 31);
INSERT INTO websoso_db.novel_genre (genre_id, novel_genre_id, novel_id) VALUES (4, 32, 32);
INSERT INTO websoso_db.novel_genre (genre_id, novel_genre_id, novel_id) VALUES (4, 33, 33);
INSERT INTO websoso_db.novel_genre (genre_id, novel_genre_id, novel_id) VALUES (4, 34, 34);
INSERT INTO websoso_db.novel_genre (genre_id, novel_genre_id, novel_id) VALUES (4, 35, 35);
INSERT INTO websoso_db.novel_genre (genre_id, novel_genre_id, novel_id) VALUES (4, 36, 36);
INSERT INTO websoso_db.novel_genre (genre_id, novel_genre_id, novel_id) VALUES (4, 37, 37);
INSERT INTO websoso_db.novel_genre (genre_id, novel_genre_id, novel_id) VALUES (4, 38, 38);
INSERT INTO websoso_db.novel_genre (genre_id, novel_genre_id, novel_id) VALUES (4, 39, 39);
INSERT INTO websoso_db.novel_genre (genre_id, novel_genre_id, novel_id) VALUES (4, 40, 40);
INSERT INTO websoso_db.novel_genre (genre_id, novel_genre_id, novel_id) VALUES (5, 41, 41);
INSERT INTO websoso_db.novel_genre (genre_id, novel_genre_id, novel_id) VALUES (5, 42, 42);
INSERT INTO websoso_db.novel_genre (genre_id, novel_genre_id, novel_id) VALUES (5, 43, 43);
INSERT INTO websoso_db.novel_genre (genre_id, novel_genre_id, novel_id) VALUES (5, 44, 44);
INSERT INTO websoso_db.novel_genre (genre_id, novel_genre_id, novel_id) VALUES (5, 45, 45);
INSERT INTO websoso_db.novel_genre (genre_id, novel_genre_id, novel_id) VALUES (5, 46, 46);
INSERT INTO websoso_db.novel_genre (genre_id, novel_genre_id, novel_id) VALUES (5, 47, 47);
INSERT INTO websoso_db.novel_genre (genre_id, novel_genre_id, novel_id) VALUES (5, 48, 48);
INSERT INTO websoso_db.novel_genre (genre_id, novel_genre_id, novel_id) VALUES (5, 49, 49);
INSERT INTO websoso_db.novel_genre (genre_id, novel_genre_id, novel_id) VALUES (5, 50, 50);
INSERT INTO websoso_db.novel_genre (genre_id, novel_genre_id, novel_id) VALUES (6, 51, 51);
INSERT INTO websoso_db.novel_genre (genre_id, novel_genre_id, novel_id) VALUES (6, 52, 52);
INSERT INTO websoso_db.novel_genre (genre_id, novel_genre_id, novel_id) VALUES (6, 53, 53);
INSERT INTO websoso_db.novel_genre (genre_id, novel_genre_id, novel_id) VALUES (6, 54, 54);
INSERT INTO websoso_db.novel_genre (genre_id, novel_genre_id, novel_id) VALUES (6, 55, 55);
INSERT INTO websoso_db.novel_genre (genre_id, novel_genre_id, novel_id) VALUES (6, 56, 56);
INSERT INTO websoso_db.novel_genre (genre_id, novel_genre_id, novel_id) VALUES (6, 57, 57);
INSERT INTO websoso_db.novel_genre (genre_id, novel_genre_id, novel_id) VALUES (6, 58, 58);
INSERT INTO websoso_db.novel_genre (genre_id, novel_genre_id, novel_id) VALUES (6, 59, 59);
INSERT INTO websoso_db.novel_genre (genre_id, novel_genre_id, novel_id) VALUES (6, 60, 60);

INSERT INTO websoso_db.novel_platform (platform_id, novel_id, novel_platform_id, novel_platform_url) VALUES (2, 1, 1, 'https://series.naver.com/novel/detail.series?productNo=1200383');
INSERT INTO websoso_db.novel_platform (platform_id, novel_id, novel_platform_id, novel_platform_url) VALUES (2, 2, 2, 'https://series.naver.com/novel/detail.series?productNo=1924178');
INSERT INTO websoso_db.novel_platform (platform_id, novel_id, novel_platform_id, novel_platform_url) VALUES (2, 3, 3, 'https://series.naver.com/novel/detail.series?productNo=1729133');
INSERT INTO websoso_db.novel_platform (platform_id, novel_id, novel_platform_id, novel_platform_url) VALUES (2, 4, 4, 'https://series.naver.com/novel/detail.series?productNo=1776477');
INSERT INTO websoso_db.novel_platform (platform_id, novel_id, novel_platform_id, novel_platform_url) VALUES (2, 5, 5, 'https://series.naver.com/novel/detail.series?productNo=2144920');
INSERT INTO websoso_db.novel_platform (platform_id, novel_id, novel_platform_id, novel_platform_url) VALUES (2, 6, 6, 'https://series.naver.com/novel/detail.series?productNo=2109424');
INSERT INTO websoso_db.novel_platform (platform_id, novel_id, novel_platform_id, novel_platform_url) VALUES (2, 7, 7, 'https://series.naver.com/novel/detail.series?productNo=1892461');
INSERT INTO websoso_db.novel_platform (platform_id, novel_id, novel_platform_id, novel_platform_url) VALUES (2, 8, 8, 'https://series.naver.com/novel/detail.series?productNo=2179508');
INSERT INTO websoso_db.novel_platform (platform_id, novel_id, novel_platform_id, novel_platform_url) VALUES (2, 9, 9, 'https://series.naver.com/novel/detail.series?productNo=2350354');
INSERT INTO websoso_db.novel_platform (platform_id, novel_id, novel_platform_id, novel_platform_url) VALUES (2, 10, 10, 'https://series.naver.com/novel/detail.series?productNo=1673229');
INSERT INTO websoso_db.novel_platform (platform_id, novel_id, novel_platform_id, novel_platform_url) VALUES (2, 11, 11, 'https://series.naver.com/novel/detail.series?productNo=1550607');
INSERT INTO websoso_db.novel_platform (platform_id, novel_id, novel_platform_id, novel_platform_url) VALUES (2, 12, 12, 'https://series.naver.com/novel/detail.series?productNo=1401611');
INSERT INTO websoso_db.novel_platform (platform_id, novel_id, novel_platform_id, novel_platform_url) VALUES (2, 13, 13, 'https://series.naver.com/novel/detail.series?productNo=2233201');
INSERT INTO websoso_db.novel_platform (platform_id, novel_id, novel_platform_id, novel_platform_url) VALUES (2, 14, 14, 'https://series.naver.com/novel/detail.series?productNo=1279606');
INSERT INTO websoso_db.novel_platform (platform_id, novel_id, novel_platform_id, novel_platform_url) VALUES (2, 15, 15, 'https://series.naver.com/novel/detail.series?productNo=2350385');
INSERT INTO websoso_db.novel_platform (platform_id, novel_id, novel_platform_id, novel_platform_url) VALUES (2, 16, 16, 'https://series.naver.com/novel/detail.series?productNo=2408008');
INSERT INTO websoso_db.novel_platform (platform_id, novel_id, novel_platform_id, novel_platform_url) VALUES (2, 17, 17, 'https://series.naver.com/novel/detail.series?productNo=1751095');
INSERT INTO websoso_db.novel_platform (platform_id, novel_id, novel_platform_id, novel_platform_url) VALUES (2, 18, 18, 'https://series.naver.com/novel/detail.series?productNo=1240893');
INSERT INTO websoso_db.novel_platform (platform_id, novel_id, novel_platform_id, novel_platform_url) VALUES (2, 19, 19, 'https://series.naver.com/novel/detail.series?productNo=1673202');
INSERT INTO websoso_db.novel_platform (platform_id, novel_id, novel_platform_id, novel_platform_url) VALUES (2, 20, 20, 'https://series.naver.com/novel/detail.series?productNo=1628522');
INSERT INTO websoso_db.novel_platform (platform_id, novel_id, novel_platform_id, novel_platform_url) VALUES (2, 21, 21, 'https://series.naver.com/novel/detail.series?productNo=1315886');
INSERT INTO websoso_db.novel_platform (platform_id, novel_id, novel_platform_id, novel_platform_url) VALUES (2, 22, 22, 'https://series.naver.com/novel/detail.series?productNo=1545770');
INSERT INTO websoso_db.novel_platform (platform_id, novel_id, novel_platform_id, novel_platform_url) VALUES (2, 23, 23, 'https://series.naver.com/novel/detail.series?productNo=1353002');
INSERT INTO websoso_db.novel_platform (platform_id, novel_id, novel_platform_id, novel_platform_url) VALUES (2, 24, 24, 'https://series.naver.com/novel/detail.series?productNo=1727697');
INSERT INTO websoso_db.novel_platform (platform_id, novel_id, novel_platform_id, novel_platform_url) VALUES (2, 25, 25, 'https://series.naver.com/novel/detail.series?productNo=1608128');
INSERT INTO websoso_db.novel_platform (platform_id, novel_id, novel_platform_id, novel_platform_url) VALUES (2, 26, 26, 'https://series.naver.com/novel/detail.series?productNo=1715691');
INSERT INTO websoso_db.novel_platform (platform_id, novel_id, novel_platform_id, novel_platform_url) VALUES (2, 27, 27, 'https://series.naver.com/novel/detail.series?productNo=1681385');
INSERT INTO websoso_db.novel_platform (platform_id, novel_id, novel_platform_id, novel_platform_url) VALUES (2, 28, 28, 'https://series.naver.com/novel/detail.series?productNo=551040');
INSERT INTO websoso_db.novel_platform (platform_id, novel_id, novel_platform_id, novel_platform_url) VALUES (2, 29, 29, 'https://series.naver.com/novel/detail.series?productNo=542778');
INSERT INTO websoso_db.novel_platform (platform_id, novel_id, novel_platform_id, novel_platform_url) VALUES (2, 30, 30, 'https://series.naver.com/novel/detail.series?productNo=1787051');
INSERT INTO websoso_db.novel_platform (platform_id, novel_id, novel_platform_id, novel_platform_url) VALUES (2, 31, 31, 'https://series.naver.com/novel/detail.series?productNo=1870800');
INSERT INTO websoso_db.novel_platform (platform_id, novel_id, novel_platform_id, novel_platform_url) VALUES (2, 32, 32, 'https://series.naver.com/novel/detail.series?productNo=2227858');
INSERT INTO websoso_db.novel_platform (platform_id, novel_id, novel_platform_id, novel_platform_url) VALUES (2, 33, 33, 'https://series.naver.com/novel/detail.series?productNo=1401609');
INSERT INTO websoso_db.novel_platform (platform_id, novel_id, novel_platform_id, novel_platform_url) VALUES (2, 34, 34, 'https://series.naver.com/novel/detail.series?productNo=1800112');
INSERT INTO websoso_db.novel_platform (platform_id, novel_id, novel_platform_id, novel_platform_url) VALUES (2, 35, 35, 'https://series.naver.com/novel/detail.series?productNo=1572242');
INSERT INTO websoso_db.novel_platform (platform_id, novel_id, novel_platform_id, novel_platform_url) VALUES (2, 36, 36, 'https://series.naver.com/novel/detail.series?productNo=2073519');
INSERT INTO websoso_db.novel_platform (platform_id, novel_id, novel_platform_id, novel_platform_url) VALUES (2, 37, 37, 'https://series.naver.com/novel/detail.series?productNo=2014221');
INSERT INTO websoso_db.novel_platform (platform_id, novel_id, novel_platform_id, novel_platform_url) VALUES (2, 38, 38, 'https://series.naver.com/novel/detail.series?productNo=2044685');
INSERT INTO websoso_db.novel_platform (platform_id, novel_id, novel_platform_id, novel_platform_url) VALUES (2, 39, 39, 'https://series.naver.com/novel/detail.series?productNo=2467854');
INSERT INTO websoso_db.novel_platform (platform_id, novel_id, novel_platform_id, novel_platform_url) VALUES (2, 40, 40, 'https://series.naver.com/novel/detail.series?productNo=1610818');
INSERT INTO websoso_db.novel_platform (platform_id, novel_id, novel_platform_id, novel_platform_url) VALUES (1, 41, 41, 'https://page.kakao.com/content/56598258?tab_type=about');
INSERT INTO websoso_db.novel_platform (platform_id, novel_id, novel_platform_id, novel_platform_url) VALUES (1, 42, 42, 'https://page.kakao.com/content/51513025?tab_type=about');
INSERT INTO websoso_db.novel_platform (platform_id, novel_id, novel_platform_id, novel_platform_url) VALUES (1, 43, 43, 'https://page.kakao.com/content/55806263?tab_type=about');
INSERT INTO websoso_db.novel_platform (platform_id, novel_id, novel_platform_id, novel_platform_url) VALUES (1, 44, 44, 'https://page.kakao.com/content/63285236?tab_type=about');
INSERT INTO websoso_db.novel_platform (platform_id, novel_id, novel_platform_id, novel_platform_url) VALUES (1, 45, 45, 'https://page.kakao.com/content/59647809?tab_type=about');
INSERT INTO websoso_db.novel_platform (platform_id, novel_id, novel_platform_id, novel_platform_url) VALUES (1, 46, 46, 'https://page.kakao.com/content/63252846?tab_type=about');
INSERT INTO websoso_db.novel_platform (platform_id, novel_id, novel_platform_id, novel_platform_url) VALUES (1, 47, 47, 'https://page.kakao.com/content/62112560?tab_type=about');
INSERT INTO websoso_db.novel_platform (platform_id, novel_id, novel_platform_id, novel_platform_url) VALUES (1, 48, 48, 'https://page.kakao.com/content/63285438?tab_type=about');
INSERT INTO websoso_db.novel_platform (platform_id, novel_id, novel_platform_id, novel_platform_url) VALUES (1, 49, 49, 'https://page.kakao.com/content/52518104?tab_type=about');
INSERT INTO websoso_db.novel_platform (platform_id, novel_id, novel_platform_id, novel_platform_url) VALUES (1, 50, 50, 'https://page.kakao.com/content/59528246?tab_type=about');
INSERT INTO websoso_db.novel_platform (platform_id, novel_id, novel_platform_id, novel_platform_url) VALUES (1, 51, 51, 'https://page.kakao.com/content/63377273?tab_type=about');
INSERT INTO websoso_db.novel_platform (platform_id, novel_id, novel_platform_id, novel_platform_url) VALUES (1, 52, 52, 'https://page.kakao.com/content/60100687?tab_type=about');
INSERT INTO websoso_db.novel_platform (platform_id, novel_id, novel_platform_id, novel_platform_url) VALUES (1, 53, 53, 'https://page.kakao.com/content/59289844?tab_type=about');
INSERT INTO websoso_db.novel_platform (platform_id, novel_id, novel_platform_id, novel_platform_url) VALUES (1, 54, 54, 'https://page.kakao.com/content/48751358?tab_type=about');
INSERT INTO websoso_db.novel_platform (platform_id, novel_id, novel_platform_id, novel_platform_url) VALUES (1, 55, 55, 'https://page.kakao.com/content/57925483?tab_type=about');
INSERT INTO websoso_db.novel_platform (platform_id, novel_id, novel_platform_id, novel_platform_url) VALUES (1, 56, 56, 'https://page.kakao.com/content/48117620?tab_type=about');
INSERT INTO websoso_db.novel_platform (platform_id, novel_id, novel_platform_id, novel_platform_url) VALUES (1, 57, 57, 'https://page.kakao.com/content/54891933?tab_type=about');
INSERT INTO websoso_db.novel_platform (platform_id, novel_id, novel_platform_id, novel_platform_url) VALUES (1, 58, 58, 'https://page.kakao.com/content/50345266?tab_type=about');
INSERT INTO websoso_db.novel_platform (platform_id, novel_id, novel_platform_id, novel_platform_url) VALUES (1, 59, 59, 'https://page.kakao.com/content/62969483?tab_type=about');
INSERT INTO websoso_db.novel_platform (platform_id, novel_id, novel_platform_id, novel_platform_url) VALUES (1, 60, 60, 'https://page.kakao.com/content/61958013?tab_type=about');

INSERT INTO websoso_db.keyword_category (keyword_category_id, keyword_category_image, keyword_category_name) VALUES (1, 'worldview_img_url', '세계관');
INSERT INTO websoso_db.keyword_category (keyword_category_id, keyword_category_image, keyword_category_name) VALUES (2, 'material_img_url', '소재');
INSERT INTO websoso_db.keyword_category (keyword_category_id, keyword_category_image, keyword_category_name) VALUES (3, 'character_img_url', '캐릭터');
INSERT INTO websoso_db.keyword_category (keyword_category_id, keyword_category_image, keyword_category_name) VALUES (4, 'relationship_img_url', '관계');
INSERT INTO websoso_db.keyword_category (keyword_category_id, keyword_category_image, keyword_category_name) VALUES (5, 'vibe_img_url', '분위기');

# INSERT INTO websoso_db.keyword (keyword_category_id, keyword_id, keyword_name) VALUES (1, 1, '이세계');
# INSERT INTO websoso_db.keyword (keyword_category_id, keyword_id, keyword_name) VALUES (1, 2, '현대');
# INSERT INTO websoso_db.keyword (keyword_category_id, keyword_id, keyword_name) VALUES (1, 3, '동양풍/사극');
# INSERT INTO websoso_db.keyword (keyword_category_id, keyword_id, keyword_name) VALUES (1, 4, '서양풍/중세시대');
# INSERT INTO websoso_db.keyword (keyword_category_id, keyword_id, keyword_name) VALUES (1, 5, '학원/아카데미');
# INSERT INTO websoso_db.keyword (keyword_category_id, keyword_id, keyword_name) VALUES (1, 6, 'SF');
# INSERT INTO websoso_db.keyword (keyword_category_id, keyword_id, keyword_name) VALUES (1, 7, '실존역사');
# INSERT INTO websoso_db.keyword (keyword_category_id, keyword_id, keyword_name) VALUES (1, 8, '대체역사');
# INSERT INTO websoso_db.keyword (keyword_category_id, keyword_id, keyword_name) VALUES (1, 9, '아포칼립스');
# INSERT INTO websoso_db.keyword (keyword_category_id, keyword_id, keyword_name) VALUES (1, 10, '오메가버스');
# INSERT INTO websoso_db.keyword (keyword_category_id, keyword_id, keyword_name) VALUES (1, 11, '가이드버스');
# INSERT INTO websoso_db.keyword (keyword_category_id, keyword_id, keyword_name) VALUES (2, 12, '웹툰화');
# INSERT INTO websoso_db.keyword (keyword_category_id, keyword_id, keyword_name) VALUES (2, 13, '드라마화');
# INSERT INTO websoso_db.keyword (keyword_category_id, keyword_id, keyword_name) VALUES (2, 14, '회귀');
# INSERT INTO websoso_db.keyword (keyword_category_id, keyword_id, keyword_name) VALUES (2, 15, '빙의');
# INSERT INTO websoso_db.keyword (keyword_category_id, keyword_id, keyword_name) VALUES (2, 16, '환생');
# INSERT INTO websoso_db.keyword (keyword_category_id, keyword_id, keyword_name) VALUES (2, 17, '정통');
# INSERT INTO websoso_db.keyword (keyword_category_id, keyword_id, keyword_name) VALUES (2, 18, '신화');
# INSERT INTO websoso_db.keyword (keyword_category_id, keyword_id, keyword_name) VALUES (2, 19, '삼국지');
# INSERT INTO websoso_db.keyword (keyword_category_id, keyword_id, keyword_name) VALUES (2, 20, '성장');
# INSERT INTO websoso_db.keyword (keyword_category_id, keyword_id, keyword_name) VALUES (2, 21, '모험');
# INSERT INTO websoso_db.keyword (keyword_category_id, keyword_id, keyword_name) VALUES (2, 22, '게임');
# INSERT INTO websoso_db.keyword (keyword_category_id, keyword_id, keyword_name) VALUES (2, 23, '헌터/레이드');
# INSERT INTO websoso_db.keyword (keyword_category_id, keyword_id, keyword_name) VALUES (2, 24, '성좌');
# INSERT INTO websoso_db.keyword (keyword_category_id, keyword_id, keyword_name) VALUES (2, 25, '던전');
# INSERT INTO websoso_db.keyword (keyword_category_id, keyword_id, keyword_name) VALUES (2, 26, '좀비');
# INSERT INTO websoso_db.keyword (keyword_category_id, keyword_id, keyword_name) VALUES (2, 27, '히어로/빌런');
# INSERT INTO websoso_db.keyword (keyword_category_id, keyword_id, keyword_name) VALUES (2, 28, '상태창/시스템');
# INSERT INTO websoso_db.keyword (keyword_category_id, keyword_id, keyword_name) VALUES (2, 29, '탑등반');
# INSERT INTO websoso_db.keyword (keyword_category_id, keyword_id, keyword_name) VALUES (2, 30, '신/종교');
# INSERT INTO websoso_db.keyword (keyword_category_id, keyword_id, keyword_name) VALUES (2, 31, '초능력');
# INSERT INTO websoso_db.keyword (keyword_category_id, keyword_id, keyword_name) VALUES (2, 32, '마법/정령');
# INSERT INTO websoso_db.keyword (keyword_category_id, keyword_id, keyword_name) VALUES (2, 33, '경영');
# INSERT INTO websoso_db.keyword (keyword_category_id, keyword_id, keyword_name) VALUES (2, 34, '생존');
# INSERT INTO websoso_db.keyword (keyword_category_id, keyword_id, keyword_name) VALUES (2, 35, '전쟁');
# INSERT INTO websoso_db.keyword (keyword_category_id, keyword_id, keyword_name) VALUES (2, 36, '연애');
# INSERT INTO websoso_db.keyword (keyword_category_id, keyword_id, keyword_name) VALUES (2, 37, '결혼');
# INSERT INTO websoso_db.keyword (keyword_category_id, keyword_id, keyword_name) VALUES (2, 38, '오해/착각');
# INSERT INTO websoso_db.keyword (keyword_category_id, keyword_id, keyword_name) VALUES (2, 39, '후회');
# INSERT INTO websoso_db.keyword (keyword_category_id, keyword_id, keyword_name) VALUES (2, 40, '질투/집착');
# INSERT INTO websoso_db.keyword (keyword_category_id, keyword_id, keyword_name) VALUES (2, 41, '구원');
# INSERT INTO websoso_db.keyword (keyword_category_id, keyword_id, keyword_name) VALUES (2, 42, '복수');
# INSERT INTO websoso_db.keyword (keyword_category_id, keyword_id, keyword_name) VALUES (2, 43, '사이다');
# INSERT INTO websoso_db.keyword (keyword_category_id, keyword_id, keyword_name) VALUES (2, 44, '권선징악');
# INSERT INTO websoso_db.keyword (keyword_category_id, keyword_id, keyword_name) VALUES (2, 45, '막장');
# INSERT INTO websoso_db.keyword (keyword_category_id, keyword_id, keyword_name) VALUES (2, 46, '호러/공포/괴담');
# INSERT INTO websoso_db.keyword (keyword_category_id, keyword_id, keyword_name) VALUES (2, 47, '범죄');
# INSERT INTO websoso_db.keyword (keyword_category_id, keyword_id, keyword_name) VALUES (2, 48, '첩보');
# INSERT INTO websoso_db.keyword (keyword_category_id, keyword_id, keyword_name) VALUES (2, 49, '조직/암흑가');
# INSERT INTO websoso_db.keyword (keyword_category_id, keyword_id, keyword_name) VALUES (2, 50, '전문직');
# INSERT INTO websoso_db.keyword (keyword_category_id, keyword_id, keyword_name) VALUES (2, 51, '재벌');
# INSERT INTO websoso_db.keyword (keyword_category_id, keyword_id, keyword_name) VALUES (2, 52, '법조계');
# INSERT INTO websoso_db.keyword (keyword_category_id, keyword_id, keyword_name) VALUES (2, 53, '메디컬');
# INSERT INTO websoso_db.keyword (keyword_category_id, keyword_id, keyword_name) VALUES (2, 54, '연예계');
# INSERT INTO websoso_db.keyword (keyword_category_id, keyword_id, keyword_name) VALUES (2, 55, '인터넷방송/BJ');
# INSERT INTO websoso_db.keyword (keyword_category_id, keyword_id, keyword_name) VALUES (2, 56, '커뮤니티');
# INSERT INTO websoso_db.keyword (keyword_category_id, keyword_id, keyword_name) VALUES (2, 57, '스포츠');
# INSERT INTO websoso_db.keyword (keyword_category_id, keyword_id, keyword_name) VALUES (2, 58, '왕족/귀족');
# INSERT INTO websoso_db.keyword (keyword_category_id, keyword_id, keyword_name) VALUES (2, 59, '육아/아기');
# INSERT INTO websoso_db.keyword (keyword_category_id, keyword_id, keyword_name) VALUES (2, 60, '임신');
# INSERT INTO websoso_db.keyword (keyword_category_id, keyword_id, keyword_name) VALUES (2, 61, 'SM');
# INSERT INTO websoso_db.keyword (keyword_category_id, keyword_id, keyword_name) VALUES (2, 62, 'TS');
# INSERT INTO websoso_db.keyword (keyword_category_id, keyword_id, keyword_name) VALUES (3, 63, '영웅');
# INSERT INTO websoso_db.keyword (keyword_category_id, keyword_id, keyword_name) VALUES (3, 64, '악당/빌런');
# INSERT INTO websoso_db.keyword (keyword_category_id, keyword_id, keyword_name) VALUES (3, 65, '먼치킨');
# INSERT INTO websoso_db.keyword (keyword_category_id, keyword_id, keyword_name) VALUES (3, 66, '천재');
# INSERT INTO websoso_db.keyword (keyword_category_id, keyword_id, keyword_name) VALUES (3, 67, '노력캐');
# INSERT INTO websoso_db.keyword (keyword_category_id, keyword_id, keyword_name) VALUES (3, 68, '인외존재');
# INSERT INTO websoso_db.keyword (keyword_category_id, keyword_id, keyword_name) VALUES (3, 69, '망나니');
# INSERT INTO websoso_db.keyword (keyword_category_id, keyword_id, keyword_name) VALUES (3, 70, '계략캐');
# INSERT INTO websoso_db.keyword (keyword_category_id, keyword_id, keyword_name) VALUES (3, 71, '조력캐');
# INSERT INTO websoso_db.keyword (keyword_category_id, keyword_id, keyword_name) VALUES (3, 72, '외국인/혼혈');
# INSERT INTO websoso_db.keyword (keyword_category_id, keyword_id, keyword_name) VALUES (3, 73, '절대선');
# INSERT INTO websoso_db.keyword (keyword_category_id, keyword_id, keyword_name) VALUES (3, 74, '착한');
# INSERT INTO websoso_db.keyword (keyword_category_id, keyword_id, keyword_name) VALUES (3, 75, '외유내강');
# INSERT INTO websoso_db.keyword (keyword_category_id, keyword_id, keyword_name) VALUES (3, 76, '나쁜남자');
# INSERT INTO websoso_db.keyword (keyword_category_id, keyword_id, keyword_name) VALUES (3, 77, '나쁜여자');
# INSERT INTO websoso_db.keyword (keyword_category_id, keyword_id, keyword_name) VALUES (3, 78, '쓰레기');
# INSERT INTO websoso_db.keyword (keyword_category_id, keyword_id, keyword_name) VALUES (3, 79, '순정캐');
# INSERT INTO websoso_db.keyword (keyword_category_id, keyword_id, keyword_name) VALUES (3, 80, '순진한');
# INSERT INTO websoso_db.keyword (keyword_category_id, keyword_id, keyword_name) VALUES (3, 81, '멍청한');
# INSERT INTO websoso_db.keyword (keyword_category_id, keyword_id, keyword_name) VALUES (3, 82, '똑똑한');
# INSERT INTO websoso_db.keyword (keyword_category_id, keyword_id, keyword_name) VALUES (3, 83, '능력있는');
# INSERT INTO websoso_db.keyword (keyword_category_id, keyword_id, keyword_name) VALUES (3, 84, '우월한');
# INSERT INTO websoso_db.keyword (keyword_category_id, keyword_id, keyword_name) VALUES (3, 85, '냉정한/냉혈한');
# INSERT INTO websoso_db.keyword (keyword_category_id, keyword_id, keyword_name) VALUES (3, 86, '무심한');
# INSERT INTO websoso_db.keyword (keyword_category_id, keyword_id, keyword_name) VALUES (3, 87, '도도한');
# INSERT INTO websoso_db.keyword (keyword_category_id, keyword_id, keyword_name) VALUES (3, 88, '까칠한');
# INSERT INTO websoso_db.keyword (keyword_category_id, keyword_id, keyword_name) VALUES (3, 89, '오만한');
# INSERT INTO websoso_db.keyword (keyword_category_id, keyword_id, keyword_name) VALUES (3, 90, '상처있는');
# INSERT INTO websoso_db.keyword (keyword_category_id, keyword_id, keyword_name) VALUES (3, 91, '사연있는');
# INSERT INTO websoso_db.keyword (keyword_category_id, keyword_id, keyword_name) VALUES (3, 92, '후회하는');
# INSERT INTO websoso_db.keyword (keyword_category_id, keyword_id, keyword_name) VALUES (3, 93, '희생하는');
# INSERT INTO websoso_db.keyword (keyword_category_id, keyword_id, keyword_name) VALUES (3, 94, '구르는');
# INSERT INTO websoso_db.keyword (keyword_category_id, keyword_id, keyword_name) VALUES (3, 95, '불쌍한');
# INSERT INTO websoso_db.keyword (keyword_category_id, keyword_id, keyword_name) VALUES (3, 96, '집착하는');
# INSERT INTO websoso_db.keyword (keyword_category_id, keyword_id, keyword_name) VALUES (3, 97, '다정한');
# INSERT INTO websoso_db.keyword (keyword_category_id, keyword_id, keyword_name) VALUES (3, 98, '조신한');
# INSERT INTO websoso_db.keyword (keyword_category_id, keyword_id, keyword_name) VALUES (3, 99, '소심한');
# INSERT INTO websoso_db.keyword (keyword_category_id, keyword_id, keyword_name) VALUES (3, 100, '절륜한');
# INSERT INTO websoso_db.keyword (keyword_category_id, keyword_id, keyword_name) VALUES (3, 101, '직진하는/적극적인');
# INSERT INTO websoso_db.keyword (keyword_category_id, keyword_id, keyword_name) VALUES (3, 102, '솔직한');
# INSERT INTO websoso_db.keyword (keyword_category_id, keyword_id, keyword_name) VALUES (3, 103, '츤데레');
# INSERT INTO websoso_db.keyword (keyword_category_id, keyword_id, keyword_name) VALUES (3, 104, '유혹하는');
# INSERT INTO websoso_db.keyword (keyword_category_id, keyword_id, keyword_name) VALUES (3, 105, '철벽치는');
# INSERT INTO websoso_db.keyword (keyword_category_id, keyword_id, keyword_name) VALUES (3, 106, '능글맞은');
# INSERT INTO websoso_db.keyword (keyword_category_id, keyword_id, keyword_name) VALUES (3, 107, '존댓말하는');
# INSERT INTO websoso_db.keyword (keyword_category_id, keyword_id, keyword_name) VALUES (3, 108, '평범한');
# INSERT INTO websoso_db.keyword (keyword_category_id, keyword_id, keyword_name) VALUES (3, 109, '털털한');
# INSERT INTO websoso_db.keyword (keyword_category_id, keyword_id, keyword_name) VALUES (3, 110, '애교있는');
# INSERT INTO websoso_db.keyword (keyword_category_id, keyword_id, keyword_name) VALUES (3, 111, '엉뚱한/사차원인');
# INSERT INTO websoso_db.keyword (keyword_category_id, keyword_id, keyword_name) VALUES (3, 112, '발랄한/명랑한');
# INSERT INTO websoso_db.keyword (keyword_category_id, keyword_id, keyword_name) VALUES (3, 113, '몸좋은');
# INSERT INTO websoso_db.keyword (keyword_category_id, keyword_id, keyword_name) VALUES (3, 114, '병약한');
# INSERT INTO websoso_db.keyword (keyword_category_id, keyword_id, keyword_name) VALUES (3, 115, '헌신적인');
# INSERT INTO websoso_db.keyword (keyword_category_id, keyword_id, keyword_name) VALUES (3, 116, '사랑꾼');
# INSERT INTO websoso_db.keyword (keyword_category_id, keyword_id, keyword_name) VALUES (3, 117, '잘우는');
# INSERT INTO websoso_db.keyword (keyword_category_id, keyword_id, keyword_name) VALUES (3, 118, '얼빠');
# INSERT INTO websoso_db.keyword (keyword_category_id, keyword_id, keyword_name) VALUES (3, 119, '귀여운');
# INSERT INTO websoso_db.keyword (keyword_category_id, keyword_id, keyword_name) VALUES (3, 120, '잔망떠는');
# INSERT INTO websoso_db.keyword (keyword_category_id, keyword_id, keyword_name) VALUES (3, 121, '예쁜/미녀');
# INSERT INTO websoso_db.keyword (keyword_category_id, keyword_id, keyword_name) VALUES (3, 122, '잘생긴/미남');
# INSERT INTO websoso_db.keyword (keyword_category_id, keyword_id, keyword_name) VALUES (3, 123, '허당');
# INSERT INTO websoso_db.keyword (keyword_category_id, keyword_id, keyword_name) VALUES (3, 124, '호구');
# INSERT INTO websoso_db.keyword (keyword_category_id, keyword_id, keyword_name) VALUES (3, 125, '걸크러시');
# INSERT INTO websoso_db.keyword (keyword_category_id, keyword_id, keyword_name) VALUES (3, 126, '동정남');
# INSERT INTO websoso_db.keyword (keyword_category_id, keyword_id, keyword_name) VALUES (3, 127, '동정녀');
# INSERT INTO websoso_db.keyword (keyword_category_id, keyword_id, keyword_name) VALUES (4, 128, '친구');
# INSERT INTO websoso_db.keyword (keyword_category_id, keyword_id, keyword_name) VALUES (4, 129, '동료');
# INSERT INTO websoso_db.keyword (keyword_category_id, keyword_id, keyword_name) VALUES (4, 130, '사제지간');
# INSERT INTO websoso_db.keyword (keyword_category_id, keyword_id, keyword_name) VALUES (4, 131, '라이벌/앙숙');
# INSERT INTO websoso_db.keyword (keyword_category_id, keyword_id, keyword_name) VALUES (4, 132, '노맨스');
# INSERT INTO websoso_db.keyword (keyword_category_id, keyword_id, keyword_name) VALUES (4, 133, '첫사랑');
# INSERT INTO websoso_db.keyword (keyword_category_id, keyword_id, keyword_name) VALUES (4, 134, '짝사랑');
# INSERT INTO websoso_db.keyword (keyword_category_id, keyword_id, keyword_name) VALUES (4, 135, '오래된연인');
# INSERT INTO websoso_db.keyword (keyword_category_id, keyword_id, keyword_name) VALUES (4, 136, '계약/정략');
# INSERT INTO websoso_db.keyword (keyword_category_id, keyword_id, keyword_name) VALUES (4, 137, '신분차이');
# INSERT INTO websoso_db.keyword (keyword_category_id, keyword_id, keyword_name) VALUES (4, 138, '혐관/애증');
# INSERT INTO websoso_db.keyword (keyword_category_id, keyword_id, keyword_name) VALUES (4, 139, '배틀연애');
# INSERT INTO websoso_db.keyword (keyword_category_id, keyword_id, keyword_name) VALUES (4, 140, '재회');
# INSERT INTO websoso_db.keyword (keyword_category_id, keyword_id, keyword_name) VALUES (4, 141, '금단의관계');
# INSERT INTO websoso_db.keyword (keyword_category_id, keyword_id, keyword_name) VALUES (4, 142, '브로맨스');
# INSERT INTO websoso_db.keyword (keyword_category_id, keyword_id, keyword_name) VALUES (4, 143, '백합/GL');
# INSERT INTO websoso_db.keyword (keyword_category_id, keyword_id, keyword_name) VALUES (4, 144, '원앤온니');
# INSERT INTO websoso_db.keyword (keyword_category_id, keyword_id, keyword_name) VALUES (4, 145, '삼각관계');
# INSERT INTO websoso_db.keyword (keyword_category_id, keyword_id, keyword_name) VALUES (4, 146, '하렘');
# INSERT INTO websoso_db.keyword (keyword_category_id, keyword_id, keyword_name) VALUES (4, 147, '나이차');
# INSERT INTO websoso_db.keyword (keyword_category_id, keyword_id, keyword_name) VALUES (4, 148, '키잡');
# INSERT INTO websoso_db.keyword (keyword_category_id, keyword_id, keyword_name) VALUES (4, 149, '역키잡');
# INSERT INTO websoso_db.keyword (keyword_category_id, keyword_id, keyword_name) VALUES (4, 150, '원나잇');
# INSERT INTO websoso_db.keyword (keyword_category_id, keyword_id, keyword_name) VALUES (4, 151, '몸정');
# INSERT INTO websoso_db.keyword (keyword_category_id, keyword_id, keyword_name) VALUES (4, 152, '여공남수');
# INSERT INTO websoso_db.keyword (keyword_category_id, keyword_id, keyword_name) VALUES (4, 153, '다공일수');
# INSERT INTO websoso_db.keyword (keyword_category_id, keyword_id, keyword_name) VALUES (4, 154, '애증');
# INSERT INTO websoso_db.keyword (keyword_category_id, keyword_id, keyword_name) VALUES (5, 155, '뻔한');
# INSERT INTO websoso_db.keyword (keyword_category_id, keyword_id, keyword_name) VALUES (5, 156, '반전있는');
# INSERT INTO websoso_db.keyword (keyword_category_id, keyword_id, keyword_name) VALUES (5, 157, '탄탄한');
# INSERT INTO websoso_db.keyword (keyword_category_id, keyword_id, keyword_name) VALUES (5, 158, '힐링되는');
# INSERT INTO websoso_db.keyword (keyword_category_id, keyword_id, keyword_name) VALUES (5, 159, '일상적인');
# INSERT INTO websoso_db.keyword (keyword_category_id, keyword_id, keyword_name) VALUES (5, 160, '현실적인');
# INSERT INTO websoso_db.keyword (keyword_category_id, keyword_id, keyword_name) VALUES (5, 161, '정치적인');
# INSERT INTO websoso_db.keyword (keyword_category_id, keyword_id, keyword_name) VALUES (5, 162, '가족적인');
# INSERT INTO websoso_db.keyword (keyword_category_id, keyword_id, keyword_name) VALUES (5, 163, '미스테리한');
# INSERT INTO websoso_db.keyword (keyword_category_id, keyword_id, keyword_name) VALUES (5, 164, '무서운');
# INSERT INTO websoso_db.keyword (keyword_category_id, keyword_id, keyword_name) VALUES (5, 165, '코믹한');
# INSERT INTO websoso_db.keyword (keyword_category_id, keyword_id, keyword_name) VALUES (5, 166, '통쾌한');
# INSERT INTO websoso_db.keyword (keyword_category_id, keyword_id, keyword_name) VALUES (5, 167, '잔잔한');
# INSERT INTO websoso_db.keyword (keyword_category_id, keyword_id, keyword_name) VALUES (5, 168, '달달한');
# INSERT INTO websoso_db.keyword (keyword_category_id, keyword_id, keyword_name) VALUES (5, 169, '애잔한');
# INSERT INTO websoso_db.keyword (keyword_category_id, keyword_id, keyword_name) VALUES (5, 170, '절절한');
# INSERT INTO websoso_db.keyword (keyword_category_id, keyword_id, keyword_name) VALUES (5, 171, '운명적인');
# INSERT INTO websoso_db.keyword (keyword_category_id, keyword_id, keyword_name) VALUES (5, 172, '피폐한');
# INSERT INTO websoso_db.keyword (keyword_category_id, keyword_id, keyword_name) VALUES (5, 173, '답답한');
# INSERT INTO websoso_db.keyword (keyword_category_id, keyword_id, keyword_name) VALUES (5, 174, '우울한');
# INSERT INTO websoso_db.keyword (keyword_category_id, keyword_id, keyword_name) VALUES (5, 175, '가벼운');
# INSERT INTO websoso_db.keyword (keyword_category_id, keyword_id, keyword_name) VALUES (5, 176, '무거운');
# INSERT INTO websoso_db.keyword (keyword_category_id, keyword_id, keyword_name) VALUES (5, 177, '용두용미');
# INSERT INTO websoso_db.keyword (keyword_category_id, keyword_id, keyword_name) VALUES (5, 178, '용두사미');

INSERT INTO websoso_db.attractive_point (attractive_point_id, attractive_point_name) VALUES (1, 'worldview');
INSERT INTO websoso_db.attractive_point (attractive_point_id, attractive_point_name) VALUES (2, 'material');
INSERT INTO websoso_db.attractive_point (attractive_point_id, attractive_point_name) VALUES (3, 'character');
INSERT INTO websoso_db.attractive_point (attractive_point_id, attractive_point_name) VALUES (4, 'relationship');
INSERT INTO websoso_db.attractive_point (attractive_point_id, attractive_point_name) VALUES (5, 'vibe');
