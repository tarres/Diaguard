package com.faltenreich.diaguard.export.pdf;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.util.Log;

import com.faltenreich.diaguard.DiaguardApplication;
import com.faltenreich.diaguard.R;
import com.faltenreich.diaguard.export.Export;
import com.faltenreich.diaguard.export.ExportCallback;
import com.faltenreich.diaguard.export.ExportFormat;
import com.faltenreich.diaguard.export.pdf.meta.PdfExportCache;
import com.faltenreich.diaguard.export.pdf.meta.PdfExportConfig;
import com.faltenreich.diaguard.export.pdf.print.PdfPage;
import com.faltenreich.diaguard.export.pdf.print.PdfPrintable;
import com.faltenreich.diaguard.export.pdf.print.PdfPrintableFactory;

import org.joda.time.Days;

import java.io.File;

public class PdfExport extends AsyncTask<Void, String, File> {

    private static final String TAG = PdfExport.class.getSimpleName();

    private PdfExportConfig config;

    public PdfExport(PdfExportConfig config) {
        this.config = config;
    }

    @Override
    protected File doInBackground(Void... params) {
        File file = Export.getExportFile(config.getContext(), ExportFormat.PDF);
        try {
            PdfExportCache cache = new PdfExportCache(config, file);
            while (cache.isDateTimeValid()) {
                if (cache.isDateTimeForNewWeek()) {
                    cache.setPage(new PdfPage(cache));
                }

                PdfPrintable printable = PdfPrintableFactory.createPrintable(cache);

                float newY = cache.getPage().getPosition().getY() + printable.getHeight();
                float maxY = cache.getPage().getEndPoint().getY();
                if (newY > maxY) {
                    cache.setPage(new PdfPage(cache));
                }
                cache.getPage().draw(printable);

                publishProgress(cache);

                cache.setDateTime(cache.getDateTime().plusDays(1));
            }
            cache.clear();
        } catch (Exception exception) {
            Log.e(TAG, exception.getMessage());
        }
        return file;
    }

    @Override
    protected void onPreExecute() {
    }

    @Override
    protected void onProgressUpdate(String... message) {
        ExportCallback callback = config.getCallback();
        if (callback != null) {
            callback.onProgress(message[0]);
        }
    }

    @Override
    protected void onPostExecute(File file) {
        super.onPostExecute(file);
        ExportCallback callback = config.getCallback();
        if (callback != null) {
            if (file != null) {
                callback.onSuccess(file, PdfExportConfig.MIME_TYPE);
            } else {
                callback.onError();
            }
        }
    }

    @SuppressLint("DefaultLocale")
    private void publishProgress(PdfExportCache cache) {
        publishProgress(String.format("%s %d/%d",
            DiaguardApplication.getContext().getString(R.string.day),
            Days.daysBetween(cache.getConfig().getDateStart(), cache.getDateTime()).getDays() + 1,
            Days.daysBetween(cache.getConfig().getDateStart(), cache.getConfig().getDateEnd()).getDays() + 1)
        );
    }
}
