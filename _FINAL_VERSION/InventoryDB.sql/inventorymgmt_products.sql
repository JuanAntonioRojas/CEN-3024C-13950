-- MySQL dump 10.13  Distrib 8.0.43, for Win64 (x86_64)
--
-- Host: localhost    Database: inventorymgmt
-- ------------------------------------------------------
-- Server version	8.0.43

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `products`
--

DROP TABLE IF EXISTS `products`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `products` (
  `sku` varchar(16) NOT NULL,
  `brand` varchar(255) DEFAULT NULL,
  `name` varchar(255) NOT NULL,
  `description` text,
  `quantity` int NOT NULL,
  `price` decimal(10,2) NOT NULL,
  `imageUrl` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`sku`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `products`
--

LOCK TABLES `products` WRITE;
/*!40000 ALTER TABLE `products` DISABLE KEYS */;
INSERT INTO `products` VALUES ('ALANGE-100074','A. Lange & Söhne','Lange 1','The Lange 1 features an asymmetrical dial layout with an outsize date, power reserve indicator, and off-center time display. A hallmark of Glashütte watchmaking, it typically has a three-quarter plate in untreated German silver and balance cock engraved by hand.',5,40000.00,'/img/5.jpg'),('ALANGE-100083','A. Lange & Söhne','Datograph Up/Down','The Datograph Up/Down is a benchmark flyback chronograph with a column-wheel, outsize date, and beautifully finished in-house movement. German silver plates, hand-engraved balance cock, and exemplary anglage make it a grail for movement enthusiasts.',14,90000.00,'/img/24.jpg'),('AUDEMA-100085','Audemars Piguet','Royal Oak Offshore Chronograph','The Royal Oak Offshore is a bold, oversized evolution of the Royal Oak, noted for its octagonal bezel with visible screws, \'Méga Tapisserie\' dial, and robust chronograph movements. Offered in steel, titanium, ceramic, and precious metals, it is a statement luxury sports watch.',1,40000.00,'/img/3.jpg'),('BLANCP-100039','Blancpain','Fifty Fathoms Automatique','Launched in 1953, the Fifty Fathoms is considered the first modern diver’s watch. It features a sapphire-capped domed bezel, excellent lume, 120-hour power reserve, and high-end finishing, bridging the gap between tool watch and luxury timepiece.',61,15000.00,'/img/18.jpg'),('BREGUE-100054','Breguet','Marine Équation Marchante 5887','The 5887 pairs a perpetual calendar, tourbillon, and running equation of time—a rare complication showing solar vs mean time. With guilloché dials, hand-finished bridges, and nautical inspiration, it represents haute horlogerie and mechanical sophistication at the highest level.',76,20000.00,'/img/21.jpg'),('BREITL-100028','Breitling','Navitimer B01 Chronograph','The Navitimer is characterized by its slide-rule bezel used for flight calculations. The B01 features Breitling’s in-house chronograph movement, contrasting sub-dials (reverse panda), and a rich aviation heritage dating back to the 1950s.',81,9000.00,'/img/12.jpg'),('BVLGAR-100033','Bvlgari','Octo Finissimo Automatic','Octo Finissimo redefined thinness with a geometric octagonal case and ultra-thin in-house movements. Its architectural lines, minimalist dial, and razor-thin profile combine Italian design with Swiss technical expertise for a modern, elegant luxury watch.',88,20000.00,'/img/4.jpg'),('CARTIE-100019','Cartier','Santos de Cartier','One of the first pilot watches, the Santos features a square case with visible screws on the bezel and bracelet. Updated with \'SmartLink\' sizing and \'QuickSwitch\' strap systems, it blends historic design with modern convenience and style.',92,7500.00,'/img/8.jpg'),('GLASHU-100008','Glashütte Original','PanoMaticLunar','“This watch features an off-center dial layout with a big date and moon phase. The movement uses a duplex swan-neck fine adjustment and a 21k gold micro-rotor. It represents rigorous German watchmaking similar to Lange but at a more accessible price point.”',10,11000.00,'/img/19.jpg'),('GRANDS-100021','Grand Seiko','Spring Drive GMT SBGE201','The SBGE201 uses Spring Drive with a GMT hand for travel functionality, combining high accuracy, smooth glide-second motion, Zaratsu polishing, and nature-inspired dials. Grand Seiko offers exceptional finishing and movement tech for discerning buyers outside Swiss tradition.',58,8000.00,'/img/22.jpg'),('HUBLOT-100051','Hublot','Big Bang Unico','The Big Bang Unico showcases a \'fusion\' of materials (ceramic, titanium, rubber, gold) in a modular, industrial case design with visible H-shaped screws. The open-worked dial reveals the in-house Unico flyback chronograph movement.',3,22000.00,'/img/14.jpg'),('JAEGER-100091','Jaeger-LeCoultre','Reverso Tribute Duoface','Created for polo players to protect the glass, the Reverso has a swiveling case. The Duoface model displays two time zones on back-to-back dials driven by a single movement—Art Deco styling meets practical travel complication.',29,12000.00,'/img/6.jpg'),('OMEGA-100048','Omega','Speedmaster Moonwatch Professional','Famous as the first watch on the Moon, the Speedmaster Professional is a manual-wind chronograph with a hesalite or sapphire crystal, tachymeter bezel, and robust movement (now Co-Axial Master Chronometer). A definitive tool watch with immense historical significance.',34,7000.00,'/img/9.jpg'),('PANERA-100077','Panerai','Luminor Marina','Panerai’s Luminor is instantly recognizable by its cushion-shaped case and patented crown-protection bridge. The Marina version includes a small seconds sub-dial at 9 o\'clock. Often large and rugged, it has military diving roots and a distinct sandwich dial.',19,8000.00,'/img/13.jpg'),('PATEK-100012','Patek Philippe','Nautilus 5711/1A','The Nautilus 5711/1A is an icon of modern luxury sports watches, designed by Gérald Genta with a porthole-inspired octagonal bezel, horizontally embossed blue dial, and integrated steel bracelet. Slim profile, Geneva finishing, and high-grade movement make it a collector favorite; discontinuation drove strong aftermarket premiums.',42,35000.00,'/img/2.jpg'),('RICH-300555','Richard Mille','RM 011 Felipe Massa','The RM 011 Felipe Massa is a high-performance flyback chronograph with skeletonized movement, tonneau case, and exotic-material construction. Designed for motorsport timing, it pairs lightweight engineering with bold aesthetics and complex horology, commanding premium pricing and collector attention.',3,175000.00,'/img/25.jpg'),('ROLEX-100062','Rolex','Submariner Date','The archetype of the modern diver’s watch. Features a unidirectional Cerachrom bezel, 300m water resistance, highly legible Chromalight display, and the robust 3235 automatic movement. Known for durability, retained value, and universal recognition.',7,10500.00,'/img/10.jpg'),('ROLEX-200101','Rolex','Day Date 40 President','The Day-Date 40 is Rolex\'s flagship dress watch offered exclusively in precious metals. It displays the day in full at 12 o\'clock and the date at 3, paired with the iconic President bracelet. Robust in-house movement, superior finishing, and strong resale demand make it a symbol of established luxury.',25,48000.00,'/img/1.jpg'),('TAGHEU-100014','TAG Heuer','Monaco Calibre 11','Famous for being worn by Steve McQueen in \'Le Mans\', the Monaco is the world’s first square waterproof automatic chronograph. The Calibre 11 edition features the crown on the left, horizontal indices, and blue dial, staying true to the 1969 original.',48,7000.00,'/img/16.jpg'),('TUDOR-100095','Tudor','Black Bay Fifty-Eight','A tribute to Tudor’s 1950s divers, the BB58 has a 39mm case, \'snowflake\' hands, and vintage styling (gilt dial accents, rivet-style bracelet). It uses an in-house COSC movement and offers a high-value alternative to its big brother, the Rolex Submariner.',22,3800.00,'/img/17.jpg'),('ULYSSE-100072','Ulysse Nardin','Freak X','The Freak X has no dial and no hands in the traditional sense; the movement itself rotates to indicate time. It uses a carousel tourbillon-like mechanism and silicon technology, showcasing avant-garde horology in a wearable 43mm case.',40,24000.00,'/img/20.jpg'),('VACHER-100056','Vacheron Constantin','Overseas Self-Winding','The Overseas is Vacheron’s luxury sports line, featuring a six-sided bezel inspired by the Maltese Cross. It includes a quick-change strap system (steel, leather, rubber) and a high-end in-house movement with a 22k gold rotor, suitable for travel and daily wear.',67,25000.00,'/img/7.jpg'),('ZENITH-100066','Zenith','Chronomaster Sport','Driven by the legendary El Primero movement (capable of 1/10th of a second measurement), the Chronomaster Sport features a ceramic bezel, tricolor sub-dials, and a modern sporty aesthetic that rivals other luxury chronographs.',99,11000.00,'/img/15.jpg');
/*!40000 ALTER TABLE `products` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2025-11-27 13:15:19
