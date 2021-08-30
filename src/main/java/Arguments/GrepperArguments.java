package Arguments;

import SpeechToText.SpeechToText;

import java.util.List;

public class GrepperArguments {

     public SpeechToText speechToText;
     public String searchString;
     public List<String> inputFilesDirs;
     public SpeechToTextArguments speechToTextArguments;

     GrepperArguments(SpeechToText speechToText, String searchString, List<String> inputFilesDirs, SpeechToTextArguments speechToTextArguments) {
          this.speechToText = speechToText;
          this.searchString = searchString;
          this.inputFilesDirs = inputFilesDirs;
          this.speechToTextArguments = speechToTextArguments;
     }

     public static GrepperArgumentsBuilder builder() {
          return new GrepperArgumentsBuilder();
     }

     public static class GrepperArgumentsBuilder {
          private SpeechToText speechToText;
          private String searchString;
          private List<String> inputFilesDirs;
          private SpeechToTextArguments speechToTextArguments;

          GrepperArgumentsBuilder() {
          }

          public GrepperArgumentsBuilder speechToText(SpeechToText speechToText) {
               this.speechToText = speechToText;
               return this;
          }

          public GrepperArgumentsBuilder searchString(String searchString) {
               this.searchString = searchString;
               return this;
          }

          public GrepperArgumentsBuilder inputFilesDirs(List<String> inputFilesDirs) {
               this.inputFilesDirs = inputFilesDirs;
               return this;
          }

          public GrepperArgumentsBuilder speechToTextArguments(SpeechToTextArguments speechToTextArguments) {
               this.speechToTextArguments = speechToTextArguments;
               return this;
          }

          public GrepperArguments build() {
               return new GrepperArguments(speechToText, searchString, inputFilesDirs, speechToTextArguments);
          }

          public String toString() {
               return "GrepperArguments.GrepperArgumentsBuilder(speechToText=" + this.speechToText + ", searchString=" + this.searchString + ", inputFilesDirs=" + this.inputFilesDirs + ", speechToTextArguments=" + this.speechToTextArguments + ")";
          }
     }
}
