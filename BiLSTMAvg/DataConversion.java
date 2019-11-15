package pl.waw.ipipan.homados.neural;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

import org.zeroturnaround.zip.ZipUtil;

import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.CoreDocument;
import edu.stanford.nlp.pipeline.CoreSentence;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;

// Uses org.zeroturnaround.zt-zip 1.13 and edu.stanford.nlp.stanford-corenlp 3.9.2
public class DataConversion {
	
	/** Converts a corpus to a format used by BiLSTMAvg or BERT baseline
	 * @param cvType which CV scenario should be used to split into training and test (used for BERT only)
	 * @param cvFold which CV fold should be used for test data (used for BERT only)
	 * @param fullCorpusDir directory with the full corpus content (domain-named zip archives)
	 * @param documentsList file listing all documents in the corpus (corpusSources.tsv)
	 * @param cvList file specifying CV fold assignment (foldsCV.tsv)
	 * @param outFile path to output file with document contents
	 * @param lengthsFile path to output file with document lengths
	 * @param outputType type of output: LSTM (for BiLSTMAvg) or BERT (for BERT baseline)
	 * @param cut10 if only every tenth document should be included
	 * @throws IOException
	 */
	public static void convertFakeNewsCorpusToTF(int cvType, int cvFold, Path fullCorpusDir, Path documentsList, Path cvList, Path outFile, Path lengthsFile, String outputType, boolean cut10)
			throws IOException {
		Properties props = new Properties();
		props.setProperty("annotators", "tokenize,ssplit");
		StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
		BufferedReader doclistReader = new BufferedReader(new FileReader(documentsList.toFile()));
		BufferedReader cvlistReader = new BufferedReader(new FileReader(cvList.toFile()));
		BufferedWriter writer = new BufferedWriter(new FileWriter(outFile.toFile()));
		BufferedWriter lengthWriter = new BufferedWriter(new FileWriter(lengthsFile.toFile()));
		doclistReader.readLine();
		cvlistReader.readLine();
		String docLine = null;
		int counter = 0;
		while ((docLine = doclistReader.readLine()) != null) {
			String[] parts = docLine.split("\t");
			String domain = parts[1];
			String docNum = parts[2];
			String fake = parts[0];
			parts = cvlistReader.readLine().split("\t");
			String fold = parts[cvType];
			if (Integer.parseInt(fold) == cvFold || (cvFold < 0 && Integer.parseInt(fold) != -cvFold))
				continue;
			counter++;
			if (cut10 & counter % 10 != 0)
				continue;
			System.out.println(domain + "/" + docNum);
			String content = new String(ZipUtil.unpackEntry(Paths.get(fullCorpusDir.toString(), domain + ".zip").toFile(), "page" + docNum + ".txt"));
			CoreDocument document = new CoreDocument(content);
			pipeline.annotate(document);
			if (outputType.equals("LSTM")) {
				for (CoreSentence sentence : document.sentences()) {
					writer.write(fake + "\t" + domain + "\t" + docNum);
					for (String part : parts)
						writer.write("\t" + part);
					for (CoreLabel token : sentence.tokens())
						writer.write("\t" + canonicalText(token));
					writer.newLine();
				}
				lengthWriter.write(document.sentences().size() + "\n");
			} else if (outputType.equals("BERT")) {
				int limit = 512;
				writer.write("DUMMY\t" + fake + "\tDUMMY\t");
				for (CoreSentence sentence : document.sentences()) {
					writer.write(sentence.text().replace("\n", "").replace("\t", "") + " ");
					limit -= sentence.tokens().size();
					if (limit < 0)
						break;
				}
				writer.newLine();
			}

		}
		doclistReader.close();
		cvlistReader.close();
		lengthWriter.close();
		writer.close();
		System.out.println("Read " + counter + " pages.");
	}

	public static boolean isNumeric(String str) {
		return str.matches("-?\\d+(\\,\\d+)?(\\.\\d+)?"); // match a number with optional '-' and decimal.
	}

	private static String canonicalText(CoreLabel token) {
		String result = token.originalText();
		if (isNumeric(result))
			result = "0";
		return result;
	}

}
