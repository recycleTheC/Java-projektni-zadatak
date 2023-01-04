package hr.java.projekt.util.files;

import hr.java.projekt.model.tvrtka.OsnovniPodatciTvrtke;
import hr.java.projekt.util.validation.iban.IBANValidationException;
import hr.java.projekt.util.validation.oib.OIBValidationException;

import java.io.*;

public class OsnovniPodatciTvrtkeFile {
    public static OsnovniPodatciTvrtke reader() throws IOException {
        OsnovniPodatciTvrtke tvrtka = new OsnovniPodatciTvrtke();
        try (BufferedReader reader = new BufferedReader(new FileReader(OsnovniPodatciTvrtke.PUTANJA_DATOTEKE))) {
            String zapis = "", opis = ""; int i = 0;
            while((zapis = reader.readLine()) != null){
                switch (i) {
                    case 0 -> tvrtka.setNaziv(zapis);
                    case 1 -> tvrtka.setAdresa(zapis);
                    case 2 -> tvrtka.setMjestoPostanskiBroj(zapis);
                    case 3 -> tvrtka.setOIB(zapis);
                    case 4 -> tvrtka.setIBAN(zapis);
                    default -> opis += zapis + "\n";
                }
                i++;
            }

            tvrtka.setOpis(opis);
        } catch (IBANValidationException | OIBValidationException e) {
            throw new RuntimeException(e);
        }

        return tvrtka;
    }

    public static void writer(OsnovniPodatciTvrtke tvrtka) throws IOException {
        try(BufferedWriter writer = new BufferedWriter(new FileWriter(OsnovniPodatciTvrtke.PUTANJA_DATOTEKE)) ) {
            writer.write(tvrtka.getNaziv() + "\n");
            writer.write(tvrtka.getAdresa() + "\n");
            writer.write(tvrtka.getMjestoPostanskiBroj() + "\n");
            writer.write(tvrtka.getOIB() + "\n");
            writer.write(tvrtka.getIBAN() + "\n");
            writer.write(tvrtka.getOpis() + "\n");
        }
    }
}
