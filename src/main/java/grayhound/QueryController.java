package grayhound;

import io.micrometer.core.annotation.Timed;
import io.micronaut.context.annotation.Parameter;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.QueryValue;
import jakarta.inject.Inject;
import nl.inl.blacklab.exceptions.InvalidQuery;
import nl.inl.blacklab.mocks.MockAnnotatedField;
import nl.inl.blacklab.mocks.MockAnnotation;
import nl.inl.blacklab.mocks.MockBlackLabIndex;
import nl.inl.blacklab.queryParser.corpusql.CorpusQueryLanguageParser;
import nl.inl.blacklab.search.BlackLabIndex;
import nl.inl.blacklab.search.QueryExecutionContext;
import nl.inl.blacklab.search.indexmetadata.*;
import nl.inl.blacklab.search.lucene.BLSpanQuery;
import nl.inl.blacklab.search.textpattern.TextPattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;

@Controller("/queries")
public class QueryController {
    private static final Logger LOGGER = LoggerFactory.getLogger(QueryController.class);
    BlackLabIndex index = new MockBlackLabIndex();

    private class FakeAnnotation extends MockAnnotation {
        FakeAnnotation(IndexMetadata md, String name) {
            super(md, name);
        }

        @Override
        public AnnotationSensitivity sensitivity(MatchSensitivity sensitivity) {
            return new AnnotationSensitivity() {

                @Override
                public Annotation annotation() {
                    return FakeAnnotation.this;
                }

                @Override
                public MatchSensitivity sensitivity() {
                    return MatchSensitivity.INSENSITIVE;
                }
            };
        }
    }

    @Get("/")
    @Timed(value = "translate")
    //Metrics accessible at /metrics/translate
    public String translate(@QueryValue String pattern) throws InvalidQuery {
        MockAnnotation annotation = new FakeAnnotation(index.metadata(), "word");
        annotation.setField(new MockAnnotatedField("contents", List.of(annotation)));
        QueryExecutionContext queryExecutionContext = new QueryExecutionContext(index, annotation, MatchSensitivity.INSENSITIVE);

        TextPattern textPattern = CorpusQueryLanguageParser.parse(pattern);
        LOGGER.info(textPattern.toString());
        BLSpanQuery blQuery = textPattern.translate(queryExecutionContext);
        LOGGER.info(blQuery.toString());
        return blQuery.toString();
    }

}
