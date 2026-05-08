import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { assessmentService } from '../../services/assessmentService';
import PageHeader from '../../components/common/PageHeader';
import Button from '../../components/common/Button';
import Spinner from '../../components/common/Spinner';
import toast from 'react-hot-toast';
import { CheckCircle, ChevronLeft, ChevronRight, Wrench } from 'lucide-react';

const MOCK_SKILLS_QUESTIONS = [
  { id: 1, text: 'How proficient are you in programming?', category: 'Technical', options: ['Beginner', 'Basic', 'Intermediate', 'Advanced', 'Expert'] },
  { id: 2, text: 'Rate your data analysis skills.', category: 'Analytical', options: ['None', 'Basic', 'Intermediate', 'Advanced', 'Expert'] },
  { id: 3, text: 'How would you rate your communication skills?', category: 'Soft Skills', options: ['Poor', 'Below Average', 'Average', 'Good', 'Excellent'] },
  { id: 4, text: 'How strong are your problem-solving abilities?', category: 'Analytical', options: ['Very Weak', 'Weak', 'Average', 'Strong', 'Very Strong'] },
  { id: 5, text: 'Rate your leadership experience.', category: 'Interpersonal', options: ['None', 'Minimal', 'Some', 'Considerable', 'Extensive'] },
  { id: 6, text: 'How comfortable are you with public speaking?', category: 'Soft Skills', options: ['Very Uncomfortable', 'Uncomfortable', 'Neutral', 'Comfortable', 'Very Comfortable'] },
  { id: 7, text: 'How well do you manage your time?', category: 'Organizational', options: ['Poor', 'Below Average', 'Average', 'Good', 'Excellent'] },
  { id: 8, text: 'Rate your creative thinking ability.', category: 'Creative', options: ['None', 'Basic', 'Moderate', 'Strong', 'Exceptional'] },
];

const SkillsAssessmentPage = () => {
  const navigate = useNavigate();
  const [questions, setQuestions] = useState([]);
  const [answers, setAnswers] = useState({});
  const [currentIndex, setCurrentIndex] = useState(0);
  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);

  useEffect(() => {
    const fetch = async () => {
      try {
        const { data } = await assessmentService.getSkillsQuestions();
        setQuestions(data);
      } catch {
        setQuestions(MOCK_SKILLS_QUESTIONS);
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
      await assessmentService.submitSkillsTest(answers);
      toast.success('Skills assessment submitted!');
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

  return (
    <div className="max-w-2xl mx-auto">
      <PageHeader title="Skills Assessment" subtitle="Rate your proficiency across different skill areas." />

      <div className="bg-white rounded-2xl border border-gray-100 p-5 mb-6">
        <div className="flex justify-between text-sm text-gray-600 mb-2">
          <span>Progress</span>
          <span className="font-semibold text-blue-600">{Object.keys(answers).length}/{questions.length} answered</span>
        </div>
        <div className="w-full bg-gray-100 rounded-full h-2.5">
          <div className="bg-blue-600 h-2.5 rounded-full transition-all duration-500" style={{ width: `${progress}%` }} />
        </div>
      </div>

      <div className="flex flex-wrap gap-2 mb-6">
        {questions.map((q, i) => (
          <button
            key={q.id}
            onClick={() => setCurrentIndex(i)}
            className={`w-8 h-8 rounded-full text-xs font-medium transition-all ${
              i === currentIndex ? 'bg-blue-600 text-white scale-110'
              : answers[q.id] ? 'bg-green-500 text-white'
              : 'bg-gray-200 text-gray-600 hover:bg-gray-300'
            }`}
          >
            {answers[q.id] ? <CheckCircle size={14} className="mx-auto" /> : i + 1}
          </button>
        ))}
      </div>

      <div className="bg-white rounded-2xl border border-gray-100 shadow-sm p-8 mb-6">
        <div className="flex items-center gap-2 text-xs text-blue-600 font-medium bg-blue-50 px-3 py-1 rounded-full w-fit mb-5">
          <Wrench size={14} /> {currentQuestion.category}
        </div>
        <h3 className="text-lg font-semibold text-gray-900 mb-6">Q{currentIndex + 1}. {currentQuestion.text}</h3>
        <div className="space-y-3">
          {currentQuestion.options.map((option, idx) => (
            <button
              key={idx}
              onClick={() => handleAnswer(currentQuestion.id, option)}
              className={`w-full text-left px-5 py-3.5 rounded-xl border-2 text-sm font-medium transition-all ${
                answers[currentQuestion.id] === option
                  ? 'border-blue-600 bg-blue-50 text-blue-700'
                  : 'border-gray-200 bg-white hover:border-blue-300 text-gray-700'
              }`}
            >
              <span className="mr-3 text-gray-400">{idx + 1}.</span> {option}
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
            <CheckCircle size={18} /> Submit Assessment
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

export default SkillsAssessmentPage;
