package pl.piotrek.main;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class HelpDialog {

    public static void show() {
        Stage helpStage = new Stage();
        helpStage.setTitle("Pomoc");
        helpStage.initModality(Modality.APPLICATION_MODAL);

        VBox root = new VBox(10);
        root.setPadding(new Insets(15));

        String helpText = "Instrukcja obsługi programu:\n\n"
                + "- Dodaj Film - dodaje główny film do odtwarzania.\n"
                + "- Odtwórz - oddtwarza główny film wraz z reklamami oraz wstępem przed filmem.\n"
                + "- Pauza - zatrzymuje oddtwarzanie w konkretnym momencie.\n"
                + "- Stop - wyłącza oddtwarzacz\n"
                + "- Korekta obrazu - dostosowywanie kolorów/jasności/kontrastu/nasycenia oddtwarzanego filmu \n"
                + "- Zarzadzaj reklamami - zarzadzanie wstępem (krótki film oddtwarzany bezpośrednio przed głównym filmem), zarządzanie reklamami (reklamy w formacie filmu oddtwarzane przed filmem oraz przed wstępem)\n"
                + "- Czas startu- jeżeli chcesz żeby film był odpalony od konkretnego momentu wpisz czas startu w formacie (Minuty:Sekundy)\n"
                + "- Dostosowywanie głośności można wykonywać za pomocą suwaka 'Głośność' oraz istnieje możliwość przewijania filmu suwakiem 'Przewijanie' \n"
                + "\nW razie problemów kontaktuj się z autorem. mail: piotrekdevelop@gmail.com";

        TextArea textArea = new TextArea(helpText);
        textArea.setWrapText(true);
        textArea.setEditable(false);

        Button closeButton = new Button("Zamknij");
        closeButton.setOnAction(e -> helpStage.close());

        root.getChildren().addAll(textArea, closeButton);

        Scene scene = new Scene(root, 400, 300);
        helpStage.setScene(scene);
        helpStage.show();
    }
}