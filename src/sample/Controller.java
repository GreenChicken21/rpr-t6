package sample;

import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.StageStyle;
import javafx.util.StringConverter;

import javax.lang.model.type.NullType;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.ResourceBundle;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Controller implements Initializable {
    public ComboBox<String> cbGrad;
    public ChoiceBox<String> cbCiklus;
    public ChoiceBox<String> cbGodina;
    public ChoiceBox<String> cbOdsjek;
    public TextField ime;
    public TextField prezime;
    public TextField index;
    public TextField jmbg;
    public TextField email;
    public DatePicker datum;
    public TextField telefon;
    public TextField adresa;
    public ToggleGroup group1;
    public CheckBox borKateg;
    private String status = "redovni";
    private TextField[] toValidate;
    private boolean[] isFormValid = new boolean[7];
    private String[] errMessages;


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Inicijalizacija comboBox i choiceBox
        cbGrad.setItems(FXCollections.observableArrayList("Sarajevo","Mostar","Tuzla", "Bihac", "Gorazde", "Zenica", "Foca"));
        cbGrad.getSelectionModel().selectFirst();
        cbCiklus.setItems(FXCollections.observableArrayList("Bachelor", "Master", "Doktorski studij", "Strucni studij"));
        cbCiklus.getSelectionModel().selectFirst();
        cbGodina.setItems(FXCollections.observableArrayList("Prva", "Druga", "Treca"));
        cbGodina.getSelectionModel().selectFirst();
        cbOdsjek.setItems(FXCollections.observableArrayList("EE", "AE", "RI", "TK"));
        cbOdsjek.getSelectionModel().selectFirst();

        // ToggleGroup listener
        group1.selectedToggleProperty().addListener((ov, t, t1) -> {
            RadioButton chk = (RadioButton)t1.getToggleGroup().getSelectedToggle(); // Cast object to radio button
            status = chk.getText();
        });

        toValidate = new TextField[] {ime, prezime, index, telefon, email, jmbg, datum.getEditor()};
        errMessages = new String[] {
                "Ime mora imati barer jedno slovo.",
                "Prezime mora imati barem jedno slovo.",
                "Index ima 5 cifara i ne pocinje nulom.",
                "Format broja telefona je 999/999-999+",
                "Format email adrese je uname@example.com",
                "JMBG mora imati 13 cifara.",
                "Format datuma je dd.mm.gggg"
        };

        ArrayList<Function<Void, Boolean>> funcs = new ArrayList<>();
        funcs.addAll(Arrays.asList(isNameValid, isPrezimeValid, isIndexValid, isPhoneValid, isEmailValid, isJmbgValid, isDateValid));

        // Check if forms are valid initially
        for(int i = 0; i < isFormValid.length; i++) {
            isFormValid[i] = funcs.get(i).apply(null);
        }

        // Add the onChange and focused listeners
        for(int i = 0; i < funcs.size(); i++) {
            int finalI = i;
            toValidate[i].textProperty().addListener((observable, oldValue, newValue) -> {
                if(funcs.get(finalI).apply(null)) {
                    toValidate[finalI].getStyleClass().removeAll("invalid");
                    toValidate[finalI].getStyleClass().add("valid");
                    isFormValid[finalI] = true;
                }else {
                    toValidate[finalI].getStyleClass().removeAll("valid");
                    toValidate[finalI].getStyleClass().add("invalid");
                    isFormValid[finalI] = false;
                }
                if(finalI == 6) {
                    try {
                        datum.setValue(datum.getConverter().fromString(newValue));
                    } catch (Exception err) {
                        //
                    }
                }
            });

            int finalI1 = i;
            toValidate[i].focusedProperty().addListener((observable, oldValue, newValue) -> {
                if(!newValue) {
                    if(isFormValid[finalI1]) {
                        toValidate[finalI].getStyleClass().removeAll("valid");
                    }
                }
            });
        }

        // Date se mora zasebno obraditi
        datum.setConverter(new StringConverter<LocalDate>()
        {
            private DateTimeFormatter dateTimeFormatter=DateTimeFormatter.ofPattern("dd.MM.yyyy");

            @Override
            public String toString(LocalDate localDate)
            {
                if(localDate==null)
                    return "";
                return dateTimeFormatter.format(localDate);
            }

            @Override
            public LocalDate fromString(String dateString)
            {
                if(dateString==null || dateString.trim().isEmpty())
                {
                    return null;
                }
                return LocalDate.parse(dateString,dateTimeFormatter);
            }
        });

    }

    private void print() {
        System.out.println("Ime: " + ime.getText());
        System.out.println("Prezime: " +  prezime.getText());
        System.out.println("Index: " + index.getText());

        System.out.println("JMBG: " + jmbg.getText());
        System.out.println("Datum rodjenja: " + datum.getValue());
        System.out.println("Grad rodjenja: " + cbGrad.getSelectionModel().getSelectedItem());

        System.out.println("Adresa: " + adresa.getText());
        System.out.println("Telefon: " + telefon.getText());
        System.out.println("email: " + email.getText());

        System.out.println("Odsjek: " + cbOdsjek.getSelectionModel().getSelectedItem());
        System.out.println("Godina: " + cbGodina.getSelectionModel().getSelectedItem());
        System.out.println("Ciklus: " + cbCiklus.getSelectionModel().getSelectedItem());
        System.out.println("Status: " + status);
        System.out.println( (borKateg.isSelected()? "Pripada" : "Ne pripada") + " posebnim borackim kategorijama." );
    }

    private Function<Void, Boolean> isNameValid = (Void) -> {
        String name = ime.getText();
        Pattern rgx = Pattern.compile(".*[a-zA-Z].*", Pattern.CASE_INSENSITIVE);
        return name.length() != 0 && name.length() <= 20 && validate(name, rgx);
    };

    private Function<Void, Boolean> isPrezimeValid = (Void) -> {
        String name = prezime.getText();
        Pattern rgx = Pattern.compile(".*[a-zA-Z].*", Pattern.CASE_INSENSITIVE);
        return name.length() != 0 && name.length() <= 20 && validate(name, rgx);
    };

    private Function<Void, Boolean> isIndexValid = (Void) -> {
        Pattern rgx = Pattern.compile("[1-9]\\d{4}\\b", Pattern.CASE_INSENSITIVE);
        return validate(index.getText(), rgx) && index.getText().length() == 5;
    };

    private Function<Void, Boolean> isJmbgValid = (Void) -> {
        Pattern rgx = Pattern.compile("\\d{13}\\b", Pattern.CASE_INSENSITIVE);
        return validate(jmbg.getText(), rgx) && jmbg.getText().length() == 13;
    };

    private Function<Void, Boolean> isEmailValid = (Void) -> {
        Pattern rgx = Pattern.compile("[A-Z0-9._+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}", Pattern.CASE_INSENSITIVE);
        return validate(email.getText(), rgx);
    };

    private Function<Void, Boolean> isPhoneValid = (Void) -> {
        Pattern rgx = Pattern.compile("\\d\\d\\d/\\d\\d\\d-\\d+", Pattern.CASE_INSENSITIVE);
        return validate(telefon.getText(), rgx) || telefon.getText().isEmpty();
    };

    private Function<Void, Boolean> isDateValid = (Void) -> {
        boolean dateBefore = datum.getValue() != null && datum.getValue().isBefore(LocalDate.now());
        System.out.println("Datum " + datum.getValue() + " " + (dateBefore? "je" : "nije") + " stariji od danasnjeg.");
        Pattern rgx = Pattern.compile("\\d\\d\\.\\d\\d\\.\\d\\d\\d\\d", Pattern.CASE_INSENSITIVE);
        return (validate(datum.getEditor().getText(), rgx) && !datum.getEditor().getText().isEmpty()) && dateBefore;
    };

    private static boolean validate(String str, Pattern regex) {
        Matcher matcher = regex.matcher(str);
        return matcher.find();
    }

    private void errorDialog(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Greska");
        alert.setHeaderText("Podaci u formi nisu ispravnog formata!");
        alert.setContentText(msg);
        alert.initStyle(StageStyle.UTILITY);
        alert.showAndWait();
    }

    public void potvrdi(ActionEvent actionEvent) {
        String finalErrMsg = "";
        boolean showErrorDialog = false;
        for(int i = 0; i < isFormValid.length; i++) {
            if(!isFormValid[i]) {
                finalErrMsg += errMessages[i] + "\n";
                showErrorDialog = true;
            }
        }

        if(showErrorDialog)
            errorDialog(finalErrMsg);
        else
            print();
    }
}