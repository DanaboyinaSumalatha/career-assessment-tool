import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { assessmentService } from '../../services/assessmentService';
import PageHeader from '../../components/common/PageHeader';
import Button from '../../components/common/Button';
import Spinner from '../../components/common/Spinner';
import toast from 'react-hot-toast';
import { CheckCircle, ChevronLeft, ChevronRight, Heart } from 'lucide-react';

const MOCK_INTEREST_QUESTIONS = [
  { id: 1, text: 'I enjoy working with tools and machines.', category: 'Realistic', options: ['Not at all', 'A little', 'Somewhat', 'Quite a bit', 'Very much'] },
  { id: 2, text: 'I like investigating and solving puzzles.', category: 'Investigative', options: ['Not at all', 'A little', 'Somewhat', 'Quite a bit', 'Very much'] },
  { id: 3, text: 'I enjoy creating art, music, or writing.', category: 'Artistic', options: ['Not at all', 'A little', 'Somewhat', 'Quite a bit', 'Very much'] },
  { id: 4, text: 'I like helping and teaching others.', category: 'Social', options: ['Not at all', 'A little', 'Somewhat', 'Quite a bit', 'Very much'] },
  { id: 5, text: 'I enjoy selling things or convincing others.', category: 'Enterprising', options: ['Not at all', 'A little', 'Somewhat', 'Quite a bit', 'Very much'] },
  { id: 6, text: 'I prefer organized and structured work.', category: 'Conventional', options: ['Not at all', 'A little', 'Somewhat', 'Quite a bit', 'Very much'] },
  { id: 7, text: 'I enjoy outdoor and physical activities.', category: 'Realistic', options: ['Not at all', 'A little', 'Somewhat', 'Quite a bit', 'Very much'] },
  { id: 8, text: 'I like analyzing data and doing research.', category: 'Investigative', options: ['Not at all', 'A little', 'Somewhat', 'Quite a bit', 'Very much'] },
  { id: 9, text: 'I enjoy expressing myself creatively.', category: 'Artistic', options: ['Not at all', 'A little', 'Somewhat', 'Quite a bit', 'Very much'] },
  { id: 10, text: 'I like working as part of a team.', category: 'Social', options: ['Not at all', 'A little', 'Somewhat', 'Quite a bit', 'Very much'] },
];

const InterestSurveyPage = () => {
  const navigate = useNavigate();
  const [questions, setQuestions] = useState([]);
  const [answers, setAnswers] = useState({});
  const [currentIndex, setCurrentIndex] = useState(0);
  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);

  useEffect(() => {
    const fetch = async () => {
      try {
        const { data } = await assessmentService.getInterestQuestions();
        setQuestions(data);
      } catch {
        setQuestions(MOCK_INTEREST_QUESTIONS);
      } finally {
        setLoading(false);
      }
    };
    fetch();
  }, []);

  const handleAnswer = (questionId, answer) => setAnswers((p) => ({ ...p, [questionId]: answer }));
  const handleNext = () => currentIndex < questions.length - 1 && setCurrentIndex((i) => i + 1);
  const handlePrev = () => currentIndex > 0 && setCurrentIndex((i) => i - 1);

  const handleSubmit = async () => {
    if (Object.keys(answers).length < questions.length) {
      toast.error('Please answer all questions.');
      return;
    }
    setSubmitting(true);
    try {
      await assessmentService.submitInterestSurvey(answers);
      toast.success('Interest survey submitted!');
      navigate('/student/results');
    } catch {
      toast.error('Submission failed. Please try again.');
    } finally {
      setSubmitting(false);
    }
  };

  if (loading) return <div className="flex justify-center py-20"><Spinner size="lg" /></div>;

  const currentQuestion = questions[currentIndex];
  const progress = Math.round((Object.keys(answers).length / questions.length) * 100);
  const isLast = currentIndex === questions.length - 1;

  const categoryColors = {
    Realistic: 'bg-green-50 text-green-600',
    Investigative: 'bg-blue-50 text-blue-600',
    Artistic: 'bg-pink-50 text-pink-600',
    Social: 'bg-orange-50 text-orange-600',
    Enterprising: 'bg-yellow-50 text-yellow-600',
    Conventional: 'bg-purple-50 text-purple-600',
  };

  return (
    <div className="max-w-2xl mx-auto">
      <PageHeader title="Interest Survey" subtitle="Tell us about your professional interests and what excites you." />

      <div className="bg-white rounded-2xl border border-gray-100 p-5 mb-6">
        <div className="flex justify-between text-sm text-gray-600 mb-2">
          <span>Progress</span>
          <span className="font-semibold text-pink-600">{Object.keys(answers).length}/{questions.length} answered</span>
        </div>
        <div className="w-full bg-gray-100 rounded-full h-2.5">
          <div className="bg-pink-500 h-2.5 rounded-full transition-all duration-500" style={{ width: `${progress}%` }} />
        </div>
      </div>

      <div className="flex flex-wrap gap-2 mb-6">
        {questions.map((q, i) => (
          <button
            key={q.id}
            onClick={() => setCurrentIndex(i)}
            className={`w-8 h-8 rounded-full text-xs font-medium transition-all ${
              i === currentIndex ? 'bg-pink-500 text-white scale-110'
              : answers[q.id] ? 'bg-green-500 text-white'
              : 'bg-gray-200 text-gray-600 hover:bg-gray-300'
            }`}
          >
            {answers[q.id] ? <CheckCircle size={14} className="mx-auto" /> : i + 1}
          </button>
        ))}
      </div>

      <div className="bg-white rounded-2xl border border-gray-100 shadow-sm p-8 mb-6">
        <div className={`flex items-center gap-2 text-xs font-medium px-3 py-1 rounded-full w-fit mb-5 ${categoryColors[currentQuestion.category] || 'bg-gray-50 text-gray-600'}`}>
          <Heart size={14} /> {currentQuestion.category}
        </div>
        <h3 className="text-lg font-semibold text-gray-900 mb-6">Q{currentIndex + 1}. {currentQuestion.text}</h3>
        <div className="grid grid-cols-1 sm:grid-cols-5 gap-3">
          {currentQuestion.options.map((option, idx) => (
            <button
              key={idx}
              onClick={() => handleAnswer(currentQuestion.id, option)}
              className={`py-3 px-2 rounded-xl border-2 text-xs font-medium text-center transition-all ${
                answers[currentQuestion.id] === option
                  ? 'border-pink-500 bg-pink-50 text-pink-700'
                  : 'border-gray-200 bg-white hover:border-pink-300 text-gray-700'
              }`}
            >
              {option}
            </button>
          ))}
        </div>
      </div>

      <div className="flex justify-between">
        <Button variant="secondary" onClick={handlePrev} disabled={currentIndex === 0}>
          <ChevronLeft size={18} /> Previous
        </Button>
        {isLast ? (
          <Button onClick={handleSubmit} loading={submitting} variant="success">
            <CheckCircle size={18} /> Submit Survey
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

export default InterestSurveyPage;
