package mx.uv.fei.appconfiguration;

import mx.uv.fei.config.annotation.etiquette.Component;
import mx.uv.fei.config.annotation.etiquette.Provide;
import mx.uv.fei.config.annotation.etiquette.Keep;
import mx.uv.fei.domain.common.CsvPractitionerParser;
import mx.uv.fei.domain.common.IFileBackup;
import mx.uv.fei.domain.common.IPractitionerParser;
import mx.uv.fei.domain.common.LocalCsvBackup;

@Component
@Keep
public class PractitionerConfig {

    @Provide
    public IFileBackup provideFileBackup() {
        return new LocalCsvBackup();
    }

    @Provide
    public IPractitionerParser providePractitionerParser() {
        return new CsvPractitionerParser();
    }
}