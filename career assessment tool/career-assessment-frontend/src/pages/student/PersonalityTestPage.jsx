import { useState, useEffect, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import { assessmentService } from '../../services/assessmentService';
import PageHeader from '../../components/common/PageHeader';
import Button from '../../components/common/Button';
import Spinner from '../../components/common/Spinner';
import toast from 'react-hot-toast';
import { CheckCircle, ChevronLeft, ChevronRight, Brain } from 'lucide-react';

// Mock questions for demo
const MOCK_QUESTIONS = [
  { id: 1, text: 'I enjoy being the center of attention.', category: 'Extraversion', options: ['Strongly Disagree', 'Disagree', 'Neutral', 'Agree', 'Strongly Agree'] },
  { id: 2, text: 'I prefer to stick to plans rather than improvise.', category: 'Conscientiousness', options: ['Strongly Disagree', 'Disagree', 'Neutral', 'Agree', 'Strongly Agree'] },
  { id: 3, text: 'I often come up with new and creative ideas.', category: 'Openness', options: ['Strongly Disagree', 'Disagree', 'Neutral', 'Agree', 'Strongly Agree'] },
  { id: 4, text: 'I tend to trust people easily.', category: 'Agreeableness', options: ['Strongly Disagree', 'Disagree', 'Neutral', 'Agree', 'Strongly Agree'] },
  { id: 5, text: 'I often feel anxious or nervous.', category: 'Neuroticism', options: ['Strongly Disagree', 'Disagree', 'Neutral', 'Agree', 'Strongly Agree'] },
  { id: 6, text: 'I enjoy socializing with many different people.', category: 'Extraversion', options: ['Strongly Disagree', 'Disagree', 'Neutral', 'Agree', 'Strongly Agree'] },
  { id: 7, text: 'I pay attention to details.', category: 'Conscientiousness', options: ['Strongly Disagree', 'Disagree', 'Neutral', 'Agree', 'Strongly Agree'] },
  { id: 8, text: 'I enjoy exploring philosophical questions.', category: 'Openness', options: ['Strongly Disagree', 'Disagree', 'Neutral', 'Agree', 'Strongly Agree'] },
  { id: 9, text: 'I find it easy to empathize with others.', category: 'Agreeableness', options: ['Strongly Disagree', 'Disagree', 'Neutral', 'Agree', 'Strongly Agree'] },
  { id: 10, text: 'I remain calm under pressure.', category: 'Neuroticism', options: ['Strongly Disagree', 'Disagree', 'Neutral', 'Agree', 'Strongly Agree'] },
];

const PersonalityTestPage = () => {
  const navigate = useNavigate();
  const [questions, setQuestions] = useState([]);
  const [answers, setAnswers] = useState({});
  const [currentIndex, setCurrentIndex] = useState(0);
  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);

  useEffect(() => {
    const fetchQuestions = async () => {
      try {
        const { data } = await assessmentService.getPersonalityQuestions();
        setQuestions(data);
      } catch {
        setQuestions(MOCK_QUESTIONS);
      } finally {
        setLoading(false);
      }
    };
    fetchQuestions();
  }, []);

  const handleAnswer = (questionId, answer) => {
    setAnswers((prev) => ({ ...prev, [questionId]: answer }));
  };

  const handleNext = () => {
    if (currentIndex < questions.length - 1) setCurrentIndex((i) => i + 1);
  };

  const handlePrev = () => {
    if (currentIndex > 0) setCurrentIndex((i) => i - 1);
  };

  const handleSubmit = async () => {
    if (Object.keys(answers).length < questions.length) {
      toast.error('Please answer all questions before submitting.');
      return;
    }
    setSubmitting(true);
    try {
      await assessmentService.submitPersonalityTest(answers);
      toast.success('Personality test submitted successfully!');
      navigate('/student/results');
    } catch {
      toast.error('Submission failed. Please try again.');
    } finally {
      setSubmitting(false);
    }
  };

  if (loading) return <div className="flex justify-center py-20"><Spinner size="lg" /></div>;

  const currentQuestion = questions[currentIndex];
  const progress = Math.round(((Object.keys(answers).length) / questions.length) * 100);
  const isLast = currentIndex === questions.length - 1;

  return (
    <div className="max-w-2xl mx-auto">
      <PageHeader
        title="Personality Assessment"
        subtitle="Answer honestly — there are no right or wrong answers."
      />

      {/* Progress */}
      <div className="bg-white rounded-2xl border border-gray-100 p-5 mb-6">
        <div className="flex justify-between text-sm text-gray-600 mb-2">
          <span>Progress</span>
          <span className="font-semibold text-indigo-600">{Object.keys(answers).length}/{questions.length} answered</span>
        </div>
        <div className="w-full bg-gray-100 rounded-full h-2.5">
          <div
            className="bg-indigo-600 h-2.5 rounded-full transition-all duration-500"
            style={{ width: `${progress}%` }}
          />
        </div>
      </div>

      {/* Question navigation dots */}
      <div className="flex flex-wrap gap-2 mb-6">
        {questions.map((q, i) => (
          <button
            key={q.id}
            onClick={() => setCurrentIndex(i)}
            className={`w-8 h-8 rounded-full text-xs font-medium transition-all ${
              i === currentIndex
                ? 'bg-indigo-600 text-white scale-110'
                : answers[q.id]
                ? 'bg-green-500 text-white'
                : 'bg-gray-200 text-gray-600 hover:bg-gray-300'
            }`}
          >
            {answers[q.id] ? <CheckCircle size={14} className="mx-auto" /> : i + 1}
          </button>
        ))}
      </div>

      {/* Current question */}
      <div className="bg-white rounded-2xl border border-gray-100 shadow-sm p-8 mb-6">
        <div className="flex items-center gap-2 text-xs text-indigo-600 font-medium bg-indigo-50 px-3 py-1 rounded-full w-fit mb-5">
          <Brain size={14} />
          {currentQuestion.category}
        </div>
        <h3 className="text-lg font-semibold text-gray-900 mb-6">
          Q{currentIndex + 1}. {currentQuestion.text}
        </h3>
        <div className="space-y-3">
          {currentQuestion.options.map((option, idx) => (
            <button
              key={idx}
              onClick={() => handleAnswer(currentQuestion.id, option)}
              className={`w-full text-left px-5 py-3.5 rounded-xl border-2 text-sm font-medium transition-all ${
                answers[currentQuestion.id] === option
                  ? 'border-indigo-600 bg-indigo-50 text-indigo-700'
                  : 'border-gray-200 bg-white hover:border-indigo-300 hover:bg-indigo-50/50 text-gray-700'
              }`}
            >
              <span className="mr-3 text-gray-400">{['A', 'B', 'C', 'D', 'E'][idx]}.</span>
              {option}
            </button>
          ))}
        </div>
      </div>

      {/* Navigation */}
      <div className="flex justify-between">
        <Button variant="secondary" onClick={handlePrev} disabled={currentIndex === 0}>
          <ChevronLeft size={18} /> Previous
        </Button>
        {isLast ? (
          <Button onClick={handleSubmit} loading={submitting} variant="success">
            <CheckCircle size={18} /> Submit Test
          </Button>
        ) : (
          <Button onClick={handleNext} disabled={!answers[currentQuestion.id]}>
            Next <ChevronRight size={18} />
          </Button>
        )}
      </div>
    </div>
  );
};

export default PersonalityTestPage;
