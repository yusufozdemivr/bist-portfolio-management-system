INSERT INTO stock (id, symbol, company_name, isin_code, sector, is_active_trading, is_bist100)
VALUES
-- Bankacılık
(gen_random_uuid(), 'AKBNK', 'Akbank', NULL, 'Bankacılık', true, true),
(gen_random_uuid(), 'GARAN', 'Garanti BBVA Bankası', NULL, 'Bankacılık', true, true),
(gen_random_uuid(), 'HALKB', 'Türkiye Halk Bankası', NULL, 'Bankacılık', true, true),
(gen_random_uuid(), 'ISCTR', 'Türkiye İş Bankası C', NULL, 'Bankacılık', true, true),
(gen_random_uuid(), 'VAKBN', 'Türkiye Vakıflar Bankası', NULL, 'Bankacılık', true, true),
(gen_random_uuid(), 'YKBNK', 'Yapı ve Kredi Bankası', NULL, 'Bankacılık', true, true),
(gen_random_uuid(), 'SKBNK', 'Şekerbank', NULL, 'Bankacılık', true, true),
(gen_random_uuid(), 'TSKB', 'Türkiye Sınai Kalkınma Bankası', NULL, 'Bankacılık', true, true),
(gen_random_uuid(), 'ALBRK', 'Albaraka Türk', NULL, 'Bankacılık', true, true),

-- Havacılık & Ulaşım
(gen_random_uuid(), 'THYAO', 'Türk Hava Yolları', NULL, 'Havacılık', true, true),
(gen_random_uuid(), 'PGSUS', 'Pegasus Hava Taşımacılığı', NULL, 'Havacılık', true, true),
(gen_random_uuid(), 'CLEBI', 'Çelebi Hava Servisi', NULL, 'Havacılık', true, true),
(gen_random_uuid(), 'TAVHL', 'TAV Havalimanları Holding', NULL, 'Havacılık', true, true),

-- Otomotiv
(gen_random_uuid(), 'TOASO', 'Tofaş Türk Otomobil Fabrikası', NULL, 'Otomotiv', true, true),
(gen_random_uuid(), 'FROTO', 'Ford Otomotiv Sanayi', NULL, 'Otomotiv', true, true),
(gen_random_uuid(), 'OTKAR', 'Otokar Otomotiv ve Savunma', NULL, 'Otomotiv', true, true),
(gen_random_uuid(), 'TTRAK', 'Türk Traktör', NULL, 'Otomotiv', true, true),
(gen_random_uuid(), 'DOAS', 'Doğuş Otomotiv', NULL, 'Otomotiv', true, true),
(gen_random_uuid(), 'KARSN', 'Karsan Otomotiv', NULL, 'Otomotiv', true, true),

-- Holding
(gen_random_uuid(), 'SAHOL', 'Hacı Ömer Sabancı Holding', NULL, 'Holding', true, true),
(gen_random_uuid(), 'KCHOL', 'Koç Holding', NULL, 'Holding', true, true),
(gen_random_uuid(), 'DOHOL', 'Doğan Şirketler Grubu Holding', NULL, 'Holding', true, true),
(gen_random_uuid(), 'TKFEN', 'Tekfen Holding', NULL, 'Holding', true, true),
(gen_random_uuid(), 'AGHOL', 'AG Anadolu Grubu Holding', NULL, 'Holding', true, true),
(gen_random_uuid(), 'ALARK', 'Alarko Holding', NULL, 'Holding', true, true),
(gen_random_uuid(), 'GSDHO', 'GSD Holding', NULL, 'Holding', true, true),

-- Enerji & Petrokimya
(gen_random_uuid(), 'TUPRS', 'Tüpraş', NULL, 'Enerji', true, true),
(gen_random_uuid(), 'PETKM', 'Petkim Petrokimya', NULL, 'Enerji', true, true),
(gen_random_uuid(), 'AKSEN', 'Aksa Enerji', NULL, 'Enerji', true, true),
(gen_random_uuid(), 'ENJSA', 'Enerjisa Enerji', NULL, 'Enerji', true, true),
(gen_random_uuid(), 'ODAS', 'Odaş Elektrik', NULL, 'Enerji', true, true),
(gen_random_uuid(), 'AYEN', 'Ayen Enerji', NULL, 'Enerji', true, true),
(gen_random_uuid(), 'AKSA', 'Aksa Akrilik', NULL, 'Enerji', true, true),

-- Telekomünikasyon
(gen_random_uuid(), 'TCELL', 'Turkcell', NULL, 'Telekomünikasyon', true, true),
(gen_random_uuid(), 'TTKOM', 'Türk Telekom', NULL, 'Telekomünikasyon', true, true),

-- Demir Çelik & Madencilik
(gen_random_uuid(), 'EREGL', 'Ereğli Demir ve Çelik', NULL, 'Demir Çelik', true, true),
(gen_random_uuid(), 'KRDMD', 'Kardemir D', NULL, 'Demir Çelik', true, true),
(gen_random_uuid(), 'KOZAL', 'Koza Altın İşletmeleri', NULL, 'Madencilik', true, true),
(gen_random_uuid(), 'KOZAA', 'Koza Anadolu Metal Madencilik', NULL, 'Madencilik', true, true),

-- Perakende & Gıda
(gen_random_uuid(), 'BIMAS', 'BİM Birleşik Mağazalar', NULL, 'Perakende', true, true),
(gen_random_uuid(), 'MGROS', 'Migros Ticaret', NULL, 'Perakende', true, true),
(gen_random_uuid(), 'SOKM', 'Şok Marketler Ticaret', NULL, 'Perakende', true, true),
(gen_random_uuid(), 'CCOLA', 'Coca-Cola İçecek', NULL, 'Gıda', true, true),
(gen_random_uuid(), 'ULKER', 'Ülker Bisküvi', NULL, 'Gıda', true, true),
(gen_random_uuid(), 'AEFES', 'Anadolu Efes Biracılık', NULL, 'Gıda', true, true),
(gen_random_uuid(), 'TATGD', 'Tat Gıda Sanayi', NULL, 'Gıda', true, true),
(gen_random_uuid(), 'BANVT', 'Banvit', NULL, 'Gıda', true, true),

-- Savunma & Teknoloji
(gen_random_uuid(), 'ASELS', 'Aselsan Elektronik', NULL, 'Savunma', true, true),
(gen_random_uuid(), 'LOGO', 'Logo Yazılım', NULL, 'Teknoloji', true, true),
(gen_random_uuid(), 'NETAS', 'Netaş Telekomünikasyon', NULL, 'Teknoloji', true, true),
(gen_random_uuid(), 'PENTA', 'Penta Teknoloji', NULL, 'Teknoloji', true, true),

-- İnşaat & Çimento
(gen_random_uuid(), 'ENKAI', 'Enka İnşaat ve Sanayi', NULL, 'İnşaat', true, true),
(gen_random_uuid(), 'CIMSA', 'Çimsa Çimento', NULL, 'Çimento', true, true),
(gen_random_uuid(), 'AKCNS', 'Akçansa Çimento', NULL, 'Çimento', true, true),
(gen_random_uuid(), 'OYAKC', 'Oyak Çimento', NULL, 'Çimento', true, true),
(gen_random_uuid(), 'BUCIM', 'Bursa Çimento', NULL, 'Çimento', true, true),

-- Cam & Kimya
(gen_random_uuid(), 'SISE', 'Türkiye Şişe ve Cam', NULL, 'Cam', true, true),
(gen_random_uuid(), 'SASA', 'SASA Polyester', NULL, 'Kimya', true, true),
(gen_random_uuid(), 'GUBRF', 'Gübre Fabrikaları', NULL, 'Kimya', true, true),
(gen_random_uuid(), 'HEKTS', 'Hektaş Ticaret', NULL, 'Kimya', true, true),

-- Beyaz Eşya & Elektronik
(gen_random_uuid(), 'ARCLK', 'Arçelik', NULL, 'Beyaz Eşya', true, true),
(gen_random_uuid(), 'VESTL', 'Vestel Elektronik', NULL, 'Beyaz Eşya', true, true),
(gen_random_uuid(), 'VESBE', 'Vestel Beyaz Eşya', NULL, 'Beyaz Eşya', true, true),

-- Lastik & Endüstriyel
(gen_random_uuid(), 'BRISA', 'Brisa Bridgestone Sabancı', NULL, 'Endüstriyel', true, true),
(gen_random_uuid(), 'KORDS', 'Kordsa Teknik Tekstil', NULL, 'Endüstriyel', true, true),
(gen_random_uuid(), 'EGEEN', 'Ege Endüstri', NULL, 'Endüstriyel', true, true),
(gen_random_uuid(), 'CEMTS', 'Çemtaş Çelik Makina', NULL, 'Endüstriyel', true, true),
(gen_random_uuid(), 'TMSN', 'Tümosan Motor', NULL, 'Endüstriyel', true, true),

-- GYO (Gayrimenkul Yatırım Ortaklığı)
(gen_random_uuid(), 'EKGYO', 'Emlak Konut GYO', NULL, 'GYO', true, true),
(gen_random_uuid(), 'ISGYO', 'İş GYO', NULL, 'GYO', true, true),
(gen_random_uuid(), 'TRGYO', 'Torunlar GYO', NULL, 'GYO', true, true),
(gen_random_uuid(), 'SNGYO', 'Sinpaş GYO', NULL, 'GYO', true, true),
(gen_random_uuid(), 'HLGYO', 'Halk GYO', NULL, 'GYO', true, true),

-- Sigorta & Finans
(gen_random_uuid(), 'TURSG', 'Türkiye Sigorta', NULL, 'Sigorta', true, true),
(gen_random_uuid(), 'ANHYT', 'Anadolu Hayat Emeklilik', NULL, 'Sigorta', true, true),
(gen_random_uuid(), 'AGESA', 'AgeSA Hayat ve Emeklilik', NULL, 'Sigorta', true, true),
(gen_random_uuid(), 'ISMEN', 'İş Yatırım Menkul Değerler', NULL, 'Finans', true, true),

-- İlaç & Sağlık
(gen_random_uuid(), 'DEVA', 'Deva Holding', NULL, 'İlaç', true, true),
(gen_random_uuid(), 'SELEC', 'Selçuk Ecza Deposu', NULL, 'İlaç', true, true),
(gen_random_uuid(), 'MPARK', 'MLP Sağlık Hizmetleri', NULL, 'Sağlık', true, true),
(gen_random_uuid(), 'GESAN', 'Giresun Sağlık', NULL, 'Sağlık', true, true),

-- Diğer Sanayi & Ticaret
(gen_random_uuid(), 'KONTR', 'Kontrolmatik Teknoloji', NULL, 'Teknoloji', true, true),
(gen_random_uuid(), 'ECZYT', 'Eczacıbaşı Yatırım Holding', NULL, 'Holding', true, true),
(gen_random_uuid(), 'GLYHO', 'Global Yatırım Holding', NULL, 'Holding', true, true),
(gen_random_uuid(), 'GOZDE', 'Gözde Girişim Sermayesi', NULL, 'Finans', true, true),
(gen_random_uuid(), 'BERA', 'Bera Holding', NULL, 'Holding', true, true),
(gen_random_uuid(), 'ECILC', 'Eczacıbaşı İlaç', NULL, 'İlaç', true, true),
(gen_random_uuid(), 'IPEKE', 'İpek Doğal Enerji', NULL, 'Enerji', true, true),
(gen_random_uuid(), 'KERVT', 'Kerevitaş Gıda', NULL, 'Gıda', true, true),
(gen_random_uuid(), 'YEOTK', 'Yeo Teknoloji', NULL, 'Teknoloji', true, true),
(gen_random_uuid(), 'RGYAS', 'Reysaş GYO', NULL, 'GYO', true, true),
(gen_random_uuid(), 'TABGD', 'Tabgıda', NULL, 'Gıda', true, true),
(gen_random_uuid(), 'QUAGR', 'QUA Granite', NULL, 'Endüstriyel', true, true),
(gen_random_uuid(), 'REEDR', 'Reedr Teknoloji', NULL, 'Teknoloji', true, true),
(gen_random_uuid(), 'MAVI', 'Mavi Giyim Sanayi', NULL, 'Perakende', true, true),
(gen_random_uuid(), 'BIOEN', 'Biotrend Çevre ve Enerji', NULL, 'Enerji', true, true),
(gen_random_uuid(), 'EUPWR', 'Europower Enerji', NULL, 'Enerji', true, true),
(gen_random_uuid(), 'SMRTG', 'Smartiks Yazılım', NULL, 'Teknoloji', true, true),
(gen_random_uuid(), 'ALFAS', 'Alfa Solar Enerji', NULL, 'Enerji', true, true),
(gen_random_uuid(), 'ASTOR' ,'Astor Enerji', NULL, 'Enerji', true, true)
    ON CONFLICT (symbol) DO NOTHING;
